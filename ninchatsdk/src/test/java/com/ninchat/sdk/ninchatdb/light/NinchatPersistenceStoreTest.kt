package com.ninchat.sdk.ninchatdb.light

import android.content.Context
import android.content.SharedPreferences
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito

class NinchatPersistenceStoreTest {
    lateinit var mockedSharedPref: SharedPreferences
    lateinit var mockedContext: Context
    lateinit var mockEdit: SharedPreferences.Editor

    @Before
    fun before(){
        mockedSharedPref = Mockito.mock(SharedPreferences::class.java)
        mockedContext = Mockito.mock(Context::class.java)
        mockEdit = Mockito.mock(SharedPreferences.Editor::class.java)
        Mockito.`when`(mockedContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockedSharedPref)
        Mockito.`when`(mockedSharedPref.edit()).thenReturn(mockEdit)
    }

    @Test
    fun `should saved data into shared preference`() {
        NinchatPersistenceStore.save("key1", "value1", mockedContext)
        Mockito.verify(mockedSharedPref, Mockito.times(1)).edit()
        Mockito.verify(mockEdit, Mockito.times(1)).putString(anyString(), anyString())
    }

    @Test
    fun `should failed to set data for empty context`() {
        NinchatPersistenceStore.save("key1", "value1", null)
        Mockito.verify(mockedSharedPref, Mockito.times(0)).edit()
        Mockito.verify(mockEdit, Mockito.times(0)).putString(anyString(), anyString())
    }

    @Test
    fun `should removed data`() {
        NinchatPersistenceStore.remove("key1", mockedContext)
        Mockito.verify(mockedSharedPref, Mockito.times(1)).edit()
        Mockito.verify(mockEdit, Mockito.times(1)).remove(anyString())
    }

    @Test
    fun `should failed to removed data for empty context`() {
        NinchatPersistenceStore.remove("key1", null)
        Mockito.verify(mockedSharedPref, Mockito.times(0)).edit()
        Mockito.verify(mockEdit, Mockito.times(0)).remove(anyString())
    }

    @Test
    fun `should retrive value from shared preference`() {
        Mockito.`when`(mockedSharedPref.getString(anyString(), any())).thenReturn("value1")
        val data = NinchatPersistenceStore.get("key1", mockedContext)
        Assert.assertEquals("value1", data)
        Mockito.verify(mockedSharedPref, Mockito.times(1)).getString(anyString(), any())
    }

    @Test
    fun `should return null when context is empty`() {
        Mockito.`when`(mockedSharedPref.getString(anyString(), any())).thenReturn("value1")

        val data = NinchatPersistenceStore.get("key1", null)
        Assert.assertNull(data)
        Mockito.verify(mockedSharedPref, Mockito.times(0)).getString(anyString(), any())
    }

    @Test
    fun `should return false for null value`() {
        Mockito.`when`(mockedSharedPref.getString(anyString(), any())).thenReturn(null)
        val data = NinchatPersistenceStore.has("key1", mockedContext)
        Assert.assertFalse(data)
        Mockito.verify(mockedSharedPref, Mockito.times(1)).getString(anyString(), any())
    }

    @Test
    fun `should return false for empty string value`() {
        Mockito.`when`(mockedSharedPref.getString(anyString(), any())).thenReturn("")
        val data = NinchatPersistenceStore.has("key1", mockedContext)
        Assert.assertFalse(data)
        Mockito.verify(mockedSharedPref, Mockito.times(1)).getString(anyString(), any())
    }

    @Test
    fun `should return false for context is null`() {
        Mockito.`when`(mockedSharedPref.getString(anyString(), any())).thenReturn("")
        val data = NinchatPersistenceStore.has("key1", null)
        Assert.assertFalse(data)
        Mockito.verify(mockedSharedPref, Mockito.times(0)).getString(anyString(), any())
    }
}