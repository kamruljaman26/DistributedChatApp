package com.chat.app.server;

import com.chat.app.model.Member;
import org.jetbrains.annotations.NotNull;

import java.util.*;

// used singleton pattern so there will only one membership manager object.
public class MembershipManager {

    private static Map<String, Member> members = new HashMap<>();
    private static MembershipManager membershipManager;
    private Member coordinator = null;

    // default constructor
    private MembershipManager() {
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
        return coordinator;
    }

    public void setCoordinator(Member member) {
        coordinator = member;
    }

    public boolean isCoordinator(Member member) {
        return coordinator.equals(member);
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
        if (coordinator == null) {
            // This is the first member, so make it the coordinator
            coordinator = member;
        }
    }

    // get member based on id
    public Member getMember(String id) {
        return members.get(id);
    }

    // return true if found by id
    public synchronized boolean removeMember(@NotNull Member member) {
        if (members.containsKey(member.getId())) {
            if (coordinator.equals(member)) {
                coordinator = null;
            }
            members.remove(member.getId());
            return true;
        }

        return false;
    }

}
