import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.Charset
import java.util.*
import kotlin.concurrent.thread

class Server {
    private var serverSocket: ServerSocket? = null
    var pool: HashSet<ServerHandler> = HashSet()

    fun start() {
        val socket = ServerSocket(9876)
        println("Hört auf Port :${socket.localPort}")
        this.serverSocket = socket
        thread {
            while (!socket.isClosed) {
                val handler = ServerHandler(this, socket.accept())
                this.pool.add(handler)
                println("Neuer Client verbunden")
                thread {
                    handler.handle()
                    this.pool.remove(handler)
                }
            }
        }
    }

    fun stop() {
        this.serverSocket?.close()
    }
}

class ServerHandler(private val server: Server, private val socket: Socket) {
    private val reader: Scanner = Scanner(socket.getInputStream())
    private val writer: OutputStream = socket.getOutputStream()

    fun sende(msg: Nachricht) {
        writer.write("${msg.wire}\n".toByteArray(Charset.defaultCharset()))
    }

    fun handle() {
        while (true) {
            var line: String
            try {
                line = reader.nextLine()
            } catch (e: NoSuchElementException) {
                break;
            }

            val msg = Nachricht.parse(line)
            if (msg == null) {
                println("Unbekannte Nachricht '$line' erhalten")
            } else {
                this.handleNachricht(msg)
            }
        }
    }

    fun handleNachricht(msg: Nachricht) {
        println("Nachricht $msg erhalten")
        when (msg) {
            is Nachricht.AnAlle -> this.server.pool.filter { it != this }.forEach {
                it.sende(msg)
            }
        }
    }
}