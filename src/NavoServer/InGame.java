//package NavoServer;
//
//import Repository.Crewmate;
//import Repository.RoomNetty;
//import Util.JsonParser;
//import io.netty.channel.ChannelHandlerContext;
//import org.json.simple.JSONObject;
//import org.json.simple.parser.ParseException;
//
//public class InGame {
//    private static JSONObject header;
//    private static JSONObject body;
//
//    public static void InGame(ChannelHandlerContext ctx, String msg) {
//        header = new JSONObject();
//        body = new JSONObject();
//
//        header.put("Header", "InGame");
//
//        try {
//            JSONObject json = JsonParser.createJson(msg);
//            body.put("Function", json.get("Function"));
//
//            if(json.get("Function").equals("6")) {
//                update(ctx, json);
//            } else {
//                body.replace("Function", "-1");
//                body.put("result", "[WRONG] 잘못된 데이터");
//                header.put("Body", body);
//                ctx.writeAndFlush(header.toJSONString() + "\n");
//                System.out.println("[WRONG] 잘못된 데이터" + "\n");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//                ctx.close();
//        }
//    }
//
//    public static void update(ChannelHandlerContext ctx, JSONObject json) { // 각 클라이언트로부터 전달받은 내용 저장
//
//        System.out.println("updateReceive " + json.toJSONString());
//        RoomNetty room = RoomNetty.getMyRoom(Integer.parseInt(json.get("code").toString()));
//        header = new JSONObject();
//        body = new JSONObject();
//        header.put("Header", "InGame");
//
//        JSONObject roomInfoJson = new JSONObject();
//        JSONObject crewmatesJson = new JSONObject();
//        roomInfoJson.put("code", room.getRoomByCode());
//        try {
////            room.update(JsonParser.createJson(json.get("update").toString()));
//            room.update(JsonParser.createJson(json.get("crewmate").toString()));
//        } catch(ParseException e) {
//            e.printStackTrace();
//        }
//
//        roomInfoJson.put("code", room.getRoomByCode());
//        for(Crewmate crewmate : room.getCrewmates())
//            crewmatesJson.put(room.getCrewmates().indexOf(crewmate), crewmate.getInitCrewmateJson());
//
//        crewmatesJson.put("crewmates_size", room.getCrewmates().size());
//        roomInfoJson.put("crewmates", crewmatesJson);
//
//        body.put("update",roomInfoJson);
//        header.put("Body",body);
//        room.getChannelGroup().writeAndFlush(header.toJSONString()+"\r\n");
//        ctx.writeAndFlush(header.toJSONString() + "\r\n");
//    }
//}