package NavoServer;

import Database.DatabaseConnection;
import Database.table.User;
import Util.JsonParser;
import io.netty.channel.ChannelHandlerContext;
import org.json.simple.JSONObject;

public class Auth {
    static DatabaseConnection db = DatabaseConnection.getConnector();

    public static void Auth(ChannelHandlerContext ctx, JSONObject json, int function) {
        try {
            JSONObject parentJson = new JSONObject();
            JSONObject childJson = new JSONObject();
            //for test
            System.out.println("Auth Received : "+json.toJSONString());
            parentJson.put("Header","Auth");
            parentJson.put("Function",function);
            switch (function) {
                case 0: //login
                    login(ctx, json, parentJson, childJson);
                    break;
                case 1: //create
                    create(ctx, json, parentJson, childJson);
                    break;
                case 2: //findID
                    findID(ctx, json, parentJson, childJson);
                    break;
                case 3: //findPW
                    findPW(ctx, json, parentJson, childJson);
                    break;
                default:
                    parentJson.replace("Function",-1);
                    childJson.put("result","[WRONG] 잘못된 데이터");
                    parentJson.put("Body",childJson);
                    ctx.writeAndFlush(parentJson.toJSONString()+"\r\n");
                    System.out.println("[WRONG] 잘못된 데이터");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void login(ChannelHandlerContext ctx, JSONObject json, JSONObject parentJson, JSONObject childJson) {
        //데이터베이스 검색을 위한 유저객체 저장
        User user = new User();
        user.setId((String)json.get("id"));
        user.setPw((String)json.get("pw"));

        //검사 후 결과값 저장
        if(db.userLogin(user)) {
            childJson.put("result",1);
            System.err.println("[SUCCESS] 로그인");
        } else {
            childJson.put("result",0);
            System.err.println("[FAIL] 로그인");
        }

        //검사결과 전송
        parentJson.put("Body",childJson);
        ctx.writeAndFlush(parentJson.toJSONString()+"\r\n");
    }

    public static void findID(ChannelHandlerContext ctx, JSONObject json, JSONObject parentJson, JSONObject childJson) {

        //데이터베이스 검색을 위한 유저객체 저장
        User user = new User();
        user.setName((String)json.get("name"));
        user.setBirth((String)json.get("birth"));
        String result = db.findID(user);

        //검사 후 결과값 저장
        if (result != null) {
            childJson.put("result",result);
            System.out.println("[SUCCESS] ID 찾기 : " + result);
        } else {
            childJson.put("result",0);
            System.err.println("[FAIL] ID 찾기");
        }

        //검사결과 전송
        parentJson.put("Body",childJson);
        ctx.writeAndFlush(parentJson.toJSONString()+"\r\n");
    }

    public static void findPW(ChannelHandlerContext ctx, JSONObject json, JSONObject parentJson, JSONObject childJson) {

        //데이터베이스 검색을 위한 유저객체 저장
        User user = new User();
        user.setId(json.get("id").toString());
        user.setName(json.get("name").toString());
        String result = db.findPW(user);

        //검사 후 결과값 저장
        if (result != null) {
            childJson.put("result",result);
            System.out.println("[SUCCESS] PW 찾기 : " + result);
        } else {
            childJson.put("result",0);
            System.err.println("[FAIL] PW 찾기");
        }

        //검사결과 전송
        parentJson.put("Body",childJson);
        ctx.writeAndFlush(parentJson.toJSONString()+"\r\n");
    }

    public static void create(ChannelHandlerContext ctx, JSONObject json, JSONObject parentJson, JSONObject childJson) {

        //데이터베이스 검색을 위한 유저객체 저장
        User user = new User();
        user.setName((String)json.get("name"));
        user.setBirth((String)json.get("birth"));
        user.setPhone((String)json.get("phone"));
        user.setId((String)json.get("id"));
        user.setPw((String)json.get("pw"));

        //검사 후 결과값 저장
        if (db.createUser(user)) {
            childJson.put("result",1);
            System.out.println("[SUCCESS] 회원가입");
        } else {
            childJson.put("result",0);
            System.err.println("[FAIL] 회원가입");
        }

        //검사결과 전송
        parentJson.put("Body",childJson);
        ctx.writeAndFlush(parentJson.toJSONString()+"\r\n");
    }

}
