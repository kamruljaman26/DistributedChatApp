package com.chat.app.server;

import com.chat.app.model.Member;

import java.util.*;

// used singleton pattern so there will only one membership manager object.
public class MembershipManager {

    private Map<String, Member> members;
    private static MembershipManager membershipManager;
    private String coordinatorId = null;

    // default constructor
    private MembershipManager() {
        members = new HashMap<>();
    }

    // return singleton object
    public static synchronized MembershipManager getInstance() {
        if (membershipManager == null) {
            membershipManager = new MembershipManager();
        }
        return membershipManager;
    }

    // return all members as list
    public List<Member> getMembers() {
        return new ArrayList<>(members.values());
    }

    // result can be null when no member in list
    public Member getCoordinator() {
        return members.get(coordinatorId);
    }

    public void setCoordinator(Member member) {
        coordinatorId = member.getId();
    }

    public boolean isCoordinator(Member member) {
        return getCoordinator().equals(member);
    }

    public boolean isEmpty() {
        return members.isEmpty();
    }

    // check id in server to find out is unique or not
    public boolean isUniqueId(String id) {
        return !members.containsKey(id);
    }

    // add members
    public void addMember(Member member) {
        members.put(member.getId(), member);
        if (coordinatorId == null) {
            // This is the first member, so make it the coordinator
            coordinatorId = member.getId();
        }
    }

    // get member based on id
    public Member getMember(String id) {
        return members.get(id);
    }

    // return true if found by id
    public synchronized boolean removeMember(String id) {
        Member m = members.remove(id);
        if (id.equals(coordinatorId)) {
            // Coordinator left, so choose a new one
            coordinatorId = null;
            for (Member member : members.values()) {
                coordinatorId = member.getId();
                break;
            }
        }
        return m != null;
    }

    // return true if found by id
    public synchronized boolean removeMember(Member member) {
        return removeMember(member.getId());
    }

}
