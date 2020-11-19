package NavoServer;

import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;

public class Login {
    public static void main(String[] args) throws Exception {
        NavoStartupUtil.runServer(8030, pipeline ->
                pipeline.addLast(new LineBasedFrameDecoder(1024, true, true))
                        .addLast(new StringDecoder(CharsetUtil.UTF_8), new StringEncoder(CharsetUtil.UTF_8))
                        //.addLast(new ChatMessageCodec(), new LoggingHandler(LogLevel.INFO))
                        //.addLast(new ChatServerHandler())
                        .addLast(new LoggingHandler(LogLevel.INFO))
                        .addLast(new LoginHandler())
        );
    }
}
