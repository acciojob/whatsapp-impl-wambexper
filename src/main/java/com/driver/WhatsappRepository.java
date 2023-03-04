package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, User> adminMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;

    private HashSet<String> userMobile;
    private int customGroupCount;
    private int messageId;

    public WhatsappRepository() {
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<>();
        this.customGroupCount = 0;
        this.messageId = 0;
    }

    public String createUser(String name, String mobile) throws Exception {
        if (userMobile.contains(mobile)) {
            throw new UserExists("User already exists");
        }
        User user = new User(name, mobile);
        userMobile.add(mobile);
        return "SUCCESS";
    }

    public Group createGroup(List<User> users) {
        // if size is 2 than it is a personal chat
        if (users.size() == 2) {
            Group group = new Group(users.get(1).getName(), 2);
            groupUserMap.put(group, users);
            groupMessageMap.put(group, new ArrayList<>());
            return group;
        } else {
            customGroupCount++;
            Group group = new Group("Group " + customGroupCount, users.size());
            groupUserMap.put(group, users);
            groupMessageMap.put(group, new ArrayList<>());
            User adm = users.get(0);
            adminMap.put(group, adm);
            return group;
        }
    }

    public int createMessage(String content) {
        messageId++;
        Message m = new Message(messageId, content);
        return messageId;
    }

    public int sendMessage(Message message, User sender, Group group) throws Exception {
        if (!groupUserMap.containsKey(group)) {
            throw new Exception("Group does not exist");
        }

        if (!groupUserMap.get(group).contains(sender)) {
            throw new Exception("You are not allowed to send message");
        }
        List<Message> messageList = groupMessageMap.get(group);
        messageList.add(message);
        groupMessageMap.put(group, messageList);

        senderMap.put(message, sender);
        return groupMessageMap.get(group).size();
    }

    public String changeAdmin(User approver, User user, Group group) throws Exception {
        if (!groupUserMap.containsKey(group)) {
            throw new Exception("Group does not exist");
        }

        if (adminMap.get(group) != approver) {
            throw new Exception("Approver does not have rights");
        }

        if (!groupUserMap.get(group).contains(user)) {
            throw new Exception("User is not a participant");
        }

        adminMap.replace(group, user);
        return "SUCCESS";
    }

    public int removeUser(User user) throws Exception {
        for (Map.Entry<Group, List<User>> map : groupUserMap.entrySet()) {
            List<User> userList = map.getValue();
            if (userList.contains(user)) {
                for (User admin : adminMap.values()) {
                    if (admin == user) {
                        throw new Exception("Cannot remove admin");
                    }
                }
                userList.remove(user);

                for (Message message : senderMap.keySet()) {
                    User usr = senderMap.get(message);
                    if (usr == user) {
                        senderMap.remove(message);
                        groupMessageMap.get(map.getKey()).remove(message);
                        return groupUserMap.get(map.getKey()).size() + groupMessageMap.get(map.getKey()).size() + senderMap.size();
                    }
                }
            }
        }
        throw new Exception("User not found");
    }

    public String findMessage(Date start, Date end, int k) throws Exception {
        TreeMap<Integer, Message> map = new TreeMap<>();
//        ArrayList<Integer> list = new ArrayList<>();
        for (Message message : senderMap.keySet()) {
            if (message.getTimestamp().compareTo(start) > 0 && message.getTimestamp().compareTo(end) < 0) {
                map.put(message.getId(), message);
//                list.add(message.getId());
            }
        }
        if (map.size() < k) {
            throw new Exception("K is greater than the number of messages");
        }
//        Collections.sort(list);
//        int n = list.get(list.size() - k);
        return map.get(map.size() - k).getContent();
    }
}