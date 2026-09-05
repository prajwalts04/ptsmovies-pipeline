package com.pts.suite.ui.screens.ssh

import com.jcraft.jsch.ChannelShell
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.InputStream
import java.io.OutputStream
import java.util.Properties

sealed class SshConnectionState {
    object Disconnected : SshConnectionState()
    data class Connecting(val host: String, val port: Int) : SshConnectionState()
    data class Connected(val host: String, val port: Int, val user: String) : SshConnectionState()
    data class Error(val message: String) : SshConnectionState()
}

/**
 * SshSessionManager provides genuine SSH-2.0 terminal socket sessions
 * using JSch to connect to Raspberry Pi port 22 with interactive PTY xterm-256color.
 */
class SshSessionManager {

    private val jsch = JSch()
    private var session: Session? = null
    private var channel: ChannelShell? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null

    private var ioJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _connectionState = MutableStateFlow<SshConnectionState>(SshConnectionState.Disconnected)
    val connectionState: StateFlow<SshConnectionState> = _connectionState.asStateFlow()

    private val _outputFlow = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val outputFlow: SharedFlow<String> = _outputFlow.asSharedFlow()

    val terminalBuffer = TerminalBuffer(maxLines = 3000)

    val isConnected: Boolean
        get() = session?.isConnected == true && channel?.isConnected == true

    /**
     * Connects to remote SSH daemon via JSch TCP socket and allocates PTY xterm-256color shell.
     */
    fun connect(
        host: String = "hub.ptsmovies.online",
        port: Int = 22,
        username: String = "prajwal",
        password: String = "",
        privateKey: String? = null,
        passphrase: String? = null,
        cols: Int = 80,
        rows: Int = 24
    ) {
        if (isConnected) disconnect()

        _connectionState.value = SshConnectionState.Connecting(host, port)
        terminalBuffer.append("\u001B[33mConnecting to $username@$host:$port (SSH-2.0)...\u001B[0m\n")

        scope.launch {
            try {
                if (!privateKey.isNullOrBlank()) {
                    val keyBytes = privateKey.toByteArray()
                    val passBytes = passphrase?.toByteArray()
                    jsch.addIdentity("pts_key", keyBytes, null, passBytes)
                }

                val newSession = jsch.getSession(username, host, port)
                if (password.isNotBlank()) {
                    newSession.setPassword(password)
                }

                val config = Properties()
                config["StrictHostKeyChecking"] = "no"
                config["PreferredAuthentications"] = "password,publickey,keyboard-interactive"
                newSession.setConfig(config)
                newSession.timeout = 15000

                newSession.connect(15000)
                session = newSession

                val shellChannel = newSession.openChannel("shell") as ChannelShell
                shellChannel.setPtyType("xterm-256color")
                shellChannel.setPtySize(cols, rows, cols * 8, rows * 16)
                shellChannel.setEnv("TERM", "xterm-256color")
                shellChannel.setEnv("COLORTERM", "truecolor")

                outputStream = shellChannel.outputStream
                inputStream = shellChannel.inputStream

                shellChannel.connect(10000)
                channel = shellChannel

                _connectionState.value = SshConnectionState.Connected(host, port, username)
                terminalBuffer.append("\u001B[32mSSH Connection Established to $host:$port ($username)\u001B[0m\n")

                startReadingOutput(shellChannel)
            } catch (e: Exception) {
                val errorMsg = e.message ?: "SSH Connection Failed"
                _connectionState.value = SshConnectionState.Error(errorMsg)
                terminalBuffer.append("\u001B[31mSSH Error: $errorMsg\u001B[0m\n")
                disconnect()
            }
        }
    }

    private fun startReadingOutput(shellChannel: ChannelShell) {
        ioJob?.cancel()
        ioJob = scope.launch {
            val inStream = inputStream ?: return@launch
            val buffer = ByteArray(4096)

            try {
                while (isActive && shellChannel.isConnected) {
                    val bytesRead = inStream.read(buffer)
                    if (bytesRead > 0) {
                        val chunk = String(buffer, 0, bytesRead, Charsets.UTF_8)
                        terminalBuffer.append(chunk)
                        _outputFlow.emit(chunk)
                    } else if (bytesRead == -1) {
                        break
                    }
                }
            } catch (e: Exception) {
                if (isActive) {
                    terminalBuffer.append("\n\u001B[31m[SSH Session Closed: ${e.message}]\u001B[0m\n")
                }
            } finally {
                _connectionState.value = SshConnectionState.Disconnected
            }
        }
    }

    /**
     * Sends raw text or commands to SSH remote PTY shell.
     */
    fun send(text: String) {
        scope.launch {
            try {
                outputStream?.let { os ->
                    os.write(text.toByteArray(Charsets.UTF_8))
                    os.flush()
                }
            } catch (e: Exception) {
                terminalBuffer.append("\n\u001B[31m[Send Error: ${e.message}]\u001B[0m\n")
            }
        }
    }

    /**
     * Sends raw byte sequence (e.g. control characters, ESC codes) to PTY.
     */
    fun sendBytes(bytes: ByteArray) {
        scope.launch {
            try {
                outputStream?.let { os ->
                    os.write(bytes)
                    os.flush()
                }
            } catch (e: Exception) {}
        }
    }

    /**
     * Resizes the remote PTY dimensions when viewport or soft keyboard changes.
     */
    fun resizePty(cols: Int, rows: Int, widthPx: Int = 0, heightPx: Int = 0) {
        try {
            val validCols = cols.coerceAtLeast(10)
            val validRows = rows.coerceAtLeast(4)
            channel?.setPtySize(validCols, validRows, widthPx.coerceAtLeast(100), heightPx.coerceAtLeast(100))
        } catch (e: Exception) {}
    }

    /**
     * Closes the active SSH channel and TCP session.
     */
    fun disconnect() {
        ioJob?.cancel()
        ioJob = null
        try {
            outputStream?.close()
        } catch (e: Exception) {}
        try {
            inputStream?.close()
        } catch (e: Exception) {}
        try {
            channel?.disconnect()
        } catch (e: Exception) {}
        try {
            session?.disconnect()
        } catch (e: Exception) {}
        channel = null
        session = null
        outputStream = null
        inputStream = null
        _connectionState.value = SshConnectionState.Disconnected
    }
}
