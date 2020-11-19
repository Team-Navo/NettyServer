package NavoServer;

import Database.DatabaseConnection;
import org.json.simple.parser.ParseException;
import Database.User;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.json.simple.JSONObject;

import java.nio.charset.Charset;

public class LoginHandler extends ChannelInboundHandlerAdapter {
    //1. 입력된 데이터를 처리하는 이벤트 핸들러 상속
    DatabaseConnection db=DatabaseConnection.getConnector();
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        //2. 데이터 순신 이벤트 처리 메서드. 클라이언트로부터 데이터의 수신이 이루어졌을때 네티가 자동으로 호출하는 이벤트 메소드
        String readMessage = ((ByteBuf) msg).toString(Charset.defaultCharset());
        //JSONObject json= NavoServer.JsonParser.createJson(readMessage);
        try {
            JSONObject json= JsonParser.createJson(readMessage);
            System.out.println("수신한 문자열 ["+readMessage +"]");
            if(json != null) {
                // 로그인
                if(json.get("Header").equals("LOGIN")) { // 파싱 데이터의 "Header"가 "login"일 떄
                    User user = new User(); // 유저 객체 생성
                    user.setId((String) json.get("id"));
                    user.setPw((String) json.get("pw"));

                    if (db.userLogin(user)) {
                        //out.println("SUCCESS");
                        ctx.write("SUCCESS");
                        System.out.println("[SUCCESS] 로그인");

                        /*InGameThread inGameThread = new InGameThread(clientSocket);
                        new Thread(inGameThread).start();
                        isThread=false;*/
                    } else {
                        //out.println("FAIL");
                        ctx.write("FAIL");
                        System.err.println("[FAIL] 로그인");
                    }
                }
                // 회원가입
                else if(json.get("Header").equals("CREATE")) {
                    User user = new User();
                    user.setName((String)json.get("name"));
                    user.setBirth((String)json.get("birth"));
                    user.setPhone((String)json.get("phone"));
                    user.setId((String)json.get("id"));
                    user.setPw((String)json.get("pw"));

                    if (db.createUser(user)) {
                        //out.println("SUCCESS");
                        ctx.write("SUCCESS");
                        System.out.println("[SUCCESS] 회원가입");
                    } else {
                        //out.println("FAIL");
                        ctx.write("FAIL");
                        System.err.println("[FAIL] 회원가입");
                    }
                }
                // id 찾기
                else if(json.get("Header").equals("ID")) {
                    User user = new User();
                    user.setName((String)json.get("name"));
                    user.setBirth((String)json.get("birth"));

                    String result = db.findID(user);
                    if ( result != null) {
                        //out.println(result);
                        ctx.write("result");
                        System.out.println("[SUCCESS] ID 찾기 : " + result);
                    } else {
                        //out.println("FAIL");
                        ctx.write("FAIL");
                        System.err.println("[FAIL] ID 찾기");
                    }
                }
                // pw 찾기
                else if(json.get("Header").equals("PW")) {
                    User user = new User();
                    user.setId((String)json.get("id"));
                    user.setName((String)json.get("name"));
                    String result = db.findPW(user);
                    if (result != null) {
                        //out.println(result);
                        ctx.write(result);
                        System.out.println("[SUCCESS] PW 찾기 : " + result);
                    } else {
                        //out.println("FAIL");
                        ctx.write("FAIL");
                        System.err.println("[FAIL] PW 찾기");
                    }
                }
                else {
                    //out.println("[WRONG] 잘못된 데이터");
                    ctx.write("[WRONG] 잘못된 데이터");
                    System.out.println("[WRONG] 잘못된 데이터");
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //3. 수신된 데이터를 가지고 있는 네티의 바이트 버퍼 객체로 부터 문자열 객체를 읽어온다.

        //ctx.write(msg);
        //4.ctx는 채널 파이프라인에 대한 이벤트를 처리한다. 여기서는 서버에 연결된 클라이언트 채널로 입력받은 데이터를 그대로 전송한다.

    }
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx){
        //6. channelRead 이벤트의 처리가 완료된 후 자동으로 수행되는 이벤트 메서드
        ctx.flush();
        //7. 채널 파이프 라인에 저장된 버퍼를 전송
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
