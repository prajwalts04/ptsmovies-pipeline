import argparse
import os
import subprocess
import requests
import json
import time

def post_callback(url, payload):
    try:
        requests.post(url, json=payload)
    except Exception as e:
        print(f"Failed to post callback: {e}")

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--job-id", required=True)
    parser.add_argument("--url", required=True)
    parser.add_argument("--callback-url", required=True)
    args = parser.parse_args()

    hf_token = os.environ.get("HF_TOKEN")
    hf_dataset = os.environ.get("HF_DATASET")

    if not hf_token or not hf_dataset:
        post_callback(args.callback_url, {
            "job_id": args.job_id,
            "status": "Failed",
            "error": "Missing HF_TOKEN or HF_DATASET",
            "stage": "github"
        })
        return

    # 1. Download stage
    post_callback(args.callback_url, {
        "job_id": args.job_id, "stage": "github", "progress": 0, "status": "Running"
    })
    
    # In a real environment, yt-dlp would be called with aria2c and output parsed.
    # For robust implementation, we use subprocess and yt-dlp.
    raw_file = "downloaded.mp4"
    cmd_dl = [
        "yt-dlp", "-o", raw_file,
        "--external-downloader", "aria2c",
        "--external-downloader-args", "-x 16 -s 16 -k 1M",
        args.url
    ]
    
    # We will simulate the progress parsing for brevity and robustness in this script.
    # A full parser would read stdout line by line.
    dl_process = subprocess.Popen(cmd_dl, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
    for line in dl_process.stdout:
        print(line, end="")
        if "[download]" in line and "%" in line:
            # Send periodic updates
            pass
    dl_process.wait()
    
    if dl_process.returncode != 0:
        post_callback(args.callback_url, {
            "job_id": args.job_id, "status": "Failed", "error": "yt-dlp download failed", "stage": "github"
        })
        return

    # 2. Compress stage
    post_callback(args.callback_url, {
        "job_id": args.job_id, "stage": "github_compress", "progress": 0, "status": "Running"
    })
    
    out_file = f"out_{args.job_id}.mp4"
    cmd_ffmpeg = [
        "ffmpeg", "-y", "-i", raw_file,
        "-c:v", "libx265", "-crf", "26", "-preset", "fast",
        "-vf", "scale=-2:480", "-c:a", "aac", "-b:a", "128k", "-map", "0",
        out_file
    ]
    
    ff_process = subprocess.Popen(cmd_ffmpeg, stderr=subprocess.PIPE, text=True)
    for line in ff_process.stderr:
        print(line, end="")
    ff_process.wait()
    
    if ff_process.returncode != 0:
        post_callback(args.callback_url, {
            "job_id": args.job_id, "status": "Failed", "error": "ffmpeg compression failed", "stage": "github_compress"
        })
        return

    # 3. HuggingFace Upload Stage
    post_callback(args.callback_url, {
        "job_id": args.job_id, "stage": "huggingface", "progress": 0, "status": "Running"
    })
    
    upload_url = f"https://huggingface.co/api/datasets/{hf_dataset}/upload/main/{out_file}"
    headers = {"Authorization": f"Bearer {hf_token}"}
    
    try:
        with open(out_file, "rb") as f:
            res = requests.post(upload_url, headers=headers, data=f)
            if res.status_code >= 400:
                raise Exception(f"HF Upload error: {res.text}")
    except Exception as e:
        post_callback(args.callback_url, {
            "job_id": args.job_id, "status": "Failed", "error": f"Upload failed: {str(e)}", "stage": "huggingface"
        })
        return
        
    # 4. Success Completion
    post_callback(args.callback_url, {
        "job_id": args.job_id, "status": "Awaiting Pi", "hf_file": out_file
    })

if __name__ == "__main__":
    main()
