package com.chat.app;

import com.chat.app.model.Member;
import com.chat.app.server.MembershipManager;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class MembershipManagerTest {

    private MembershipManager membershipManager;

    @Before
    public void setUp() throws IOException {
        membershipManager = MembershipManager.getInstance();
    }

    @Test
    public void testAddMember() {
        Member member = new Member("1", "John Doe", 1234, 12345);
        membershipManager.addMember(member);
        assertEquals(member, membershipManager.getMember("1"));
    }

    @Test
    public void testRemoveMemberById() {
        Member member = new Member("1", "John Doe", 1234, 12345);
        membershipManager.addMember(member);
        assertTrue(membershipManager.removeMember(member));
        assertNull(membershipManager.getMember("1"));
    }

    @Test
    public void testRemoveMemberByObject() {
        Member member = new Member("1", "John Doe", 1234, 12345);
        membershipManager.addMember(member);
        assertTrue(membershipManager.removeMember(member));
        assertNull(membershipManager.getMember("1"));
    }

    @Test
    public void testSetCoordinator() {
        Member member1 = new Member("1", "John Doe", 1234, 12345);
        Member member2 = new Member("2", "John Lio", 1234, 54321);
        membershipManager.addMember(member1);
        membershipManager.addMember(member2);
        membershipManager.setCoordinator(member2);
        assertEquals(member2, membershipManager.getCoordinator());
        assertTrue(membershipManager.isCoordinator(member2));
        assertFalse(membershipManager.isCoordinator(member1));
    }
}

