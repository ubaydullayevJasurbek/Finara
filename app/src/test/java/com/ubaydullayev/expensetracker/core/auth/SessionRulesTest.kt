package com.ubaydullayev.expensetracker.core.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionRulesTest {

    @Test
    fun `first sign-in on a device does not wipe data`() {
        assertFalse(SessionRules.isDifferentOwner(storedUid = null, newUid = "a"))
    }

    @Test
    fun `same account signing in again keeps data`() {
        assertFalse(SessionRules.isDifferentOwner(storedUid = "a", newUid = "a"))
    }

    @Test
    fun `another account signing in wipes data`() {
        assertTrue(SessionRules.isDifferentOwner(storedUid = "a", newUid = "b"))
    }

    @Test
    fun `name edited by the same user is kept`() {
        val name = SessionRules.resolveName("Jasur", sameOwner = true, firebaseName = "Jasurbek U", email = "j@x.com")
        assertEquals("Jasur", name)
    }

    @Test
    fun `previous owner's name is replaced by the new account's name`() {
        val name = SessionRules.resolveName("Ali", sameOwner = false, firebaseName = "Vali", email = "v@x.com")
        assertEquals("Vali", name)
    }

    @Test
    fun `legacy placeholder name is replaced`() {
        val name = SessionRules.resolveName("Your Name", sameOwner = true, firebaseName = "Jasurbek", email = null)
        assertEquals("Jasurbek", name)
    }

    @Test
    fun `falls back to the email prefix when there is no name`() {
        val name = SessionRules.resolveName("", sameOwner = true, firebaseName = "  ", email = "jasur.dev@gmail.com")
        assertEquals("jasur.dev", name)
    }

    @Test
    fun `empty when there is neither name nor email`() {
        assertEquals("", SessionRules.resolveName("", sameOwner = false, firebaseName = null, email = null))
    }
}
