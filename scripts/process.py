import os, sys, json, time, subprocess, re, requests, tempfile, shutil
from pathlib import Path
from huggingface_hub import HfApi

JOB_ID = os.environ["JOB_ID"]
DOWNLOAD_URL = os.environ["DOWNLOAD_URL"]
IMDB_ID = os.environ["IMDB_ID"]
TITLE = os.environ["TITLE"]
CONTENT_TYPE = os.environ["CONTENT_TYPE"]
LABEL = os.environ["LABEL"]
VPS_WEBHOOK = os.environ["VPS_WEBHOOK_URL"]
SECRET = os.environ["WEBHOOK_SECRET"]
HF_TOKEN = os.environ["HF_TOKEN"]
HF_REPO = os.environ["HF_REPO_ID"]

WORK_DIR = Path(tempfile.mkdtemp())

def post_progress(stage, progress, speed="", eta="", error=None, hf_path=None):
    payload = {"job_id": JOB_ID, "stage": stage, "progress": progress, "speed": speed, "eta": eta, "error": error, "hf_path": hf_path, "secret": SECRET}
    try: requests.post(VPS_WEBHOOK, json=payload, timeout=10)
    except Exception as e: print(f"Webhook failed: {e}")

def _dl_aria2():
    cmd = ["aria2c", "-x", "16", "-s", "16", "-k", "1M", "--max-connection-per-server=16", "--split=16", "--file-allocation=none", "--user-agent=Mozilla/5.0", "-d", str(WORK_DIR), "-o", "source.mkv", DOWNLOAD_URL]
    proc = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
    buf = ""
    while True:
        ch = proc.stdout.read(1)
        if ch == "" and proc.poll() is not None: break
        if ch in ("\r", "\n"):
            m = re.search(r"\((\d+(?:\.\d+)?)%.*?DL:([\d\.]+[KMG]?i?B/s)", buf)
            if m:
                em = re.search(r"ETA:([\w]+)", buf)
                post_progress("download", float(m.group(1)), m.group(2), em.group(1) if em else "")
            buf = ""
        else:
            buf += ch
            if len(buf) > 400: buf = buf[-400:]
    proc.wait()
    f = WORK_DIR / "source.mkv"
    if proc.returncode != 0 or not f.exists() or f.stat().st_size == 0: raise RuntimeError("aria2 failed")
    return f

def _dl_ytdlp():
    out_template = str(WORK_DIR / "source.%(ext)s")
    cmd = ["yt-dlp", "-f", "bv*+ba/b", "--merge-output-format", "mkv", "-o", out_template, DOWNLOAD_URL]
    proc = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
    last_pct = 0
    for line in proc.stdout:
        print(line.strip())
        m = re.search(r"\[download\]\s+([\d\.]+)%\s+of\s+~?([\d\.\w]+)\s+at\s+([\d\.\w]+/s)\s+ETA\s+([\d\:]+)", line)
        if m:
            pct = float(m.group(1))
            if pct > last_pct:
                post_progress("download", pct, m.group(3), m.group(4))
                last_pct = pct
    proc.wait()
    if proc.returncode != 0: raise RuntimeError("yt-dlp failed")
    files = list(WORK_DIR.glob("source.*"))
    if not files: raise RuntimeError("No source file")
    return files[0]

def run_download():
    post_progress("download", 0)
    direct = bool(re.search(r"\.(mp4|mkv|avi|mov|webm|m4v)(\?|#|$)", DOWNLOAD_URL, re.I))
    if direct:
        try: return _dl_aria2()
        except Exception as e:
            print("aria2 failed, fallback yt-dlp:", e); return _dl_ytdlp()
    try: return _dl_ytdlp()
    except Exception as e:
        print("yt-dlp failed, fallback aria2:", e); return _dl_aria2()

def run_compress(src):
    post_progress("compress", 0)
    out = WORK_DIR / "final.mp4"
    cmd = ["ffmpeg", "-y", "-i", str(src), "-map", "0", "-c:v", "libx265", "-crf", "26", "-preset", "medium", "-vf", "scale=-2:480", "-c:a", "aac", "-b:a", "128k", "-c:s", "mov_text", "-tag:v", "hvc1", "-movflags", "+faststart", str(out)]
    proc = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
    probe = subprocess.run(["ffprobe", "-v", "error", "-show_entries", "format=duration", "-of", "default=noprint_wrappers=1:nokey=1", str(src)], capture_output=True, text=True)
    try: total_dur = float(probe.stdout.strip())
    except Exception: total_dur = 0
    last_pct = 0.0; last_post = 0.0
    for line in proc.stdout:
        m = re.search(r"time=(\d+):(\d+):(\d+\.\d+)", line)
        sm = re.search(r"speed=\s*([\d\.]+)x", line)
        if m and total_dur > 0:
            cur = float(m.group(1))*3600 + float(m.group(2))*60 + float(m.group(3))
            pct = min(99.9, cur/total_dur*100)
            spd = ""; eta = ""
            if sm:
                spd = sm.group(1) + "x"
                try:
                    rem = (total_dur - cur)/float(sm.group(1))
                    eta = "%dM%02dS" % (int(rem//60), int(rem%60))
                except Exception: eta = ""
            if pct - last_pct >= 0.5 or time.time() - last_post > 3:
                post_progress("compress", pct, spd, eta)
                last_pct = pct; last_post = time.time()
    proc.wait()
    if proc.returncode != 0 or not out.exists(): raise RuntimeError("Compression failed")
    post_progress("compress", 100)
    return out

def run_upload(src):
    post_progress("upload", 0)
    api = HfApi()
    safe_title = re.sub(r"[^\w\-_.]", "_", TITLE)
    safe_label = re.sub(r"[^\w\-_.]", "_", LABEL)
    path_in_repo = f"{IMDB_ID}_{safe_title}_{safe_label}.mkv"
    post_progress("upload", 50)
    api.upload_file(path_or_fileobj=str(src), path_in_repo=path_in_repo, repo_id=HF_REPO, repo_type="dataset", token=HF_TOKEN, commit_message=f"Add {TITLE} {LABEL}")
    post_progress("upload", 100)
    return path_in_repo

try:
    src = run_download()
    compressed = run_compress(src)
    hf_path = run_upload(compressed)
    post_progress("done", 100, hf_path=hf_path)
    print("Pipeline SUCCESS")
except Exception as e:
    import traceback; traceback.print_exc()
    post_progress("failed", 0, error=str(e))
    sys.exit(1)
finally:
    shutil.rmtree(WORK_DIR, ignore_errors=True)
