package com.ninchat.sdk.ninchatquestionnaire.ninchatradiobutton.presenter

import org.json.JSONObject
import org.junit.Assert
import org.junit.Test

class NinchatRadioButtonPresenterTest {
    @Test
    fun should_render_view_with_given_params() {
        val viewCallback = object : INinchatRadioButtonPresenter {
            override fun renderView(label: String, isSelected: Boolean, hasError: Boolean) {
                Assert.assertEquals("test-label", label)
                Assert.assertEquals(true, isSelected)
                Assert.assertEquals(true, hasError)
            }

            override fun onSelected() {
                Assert.assertEquals("Should not be called", true)
            }

            override fun onUnSelected() {
                Assert.assertEquals("Should not be called", true)
            }

            override fun onError() {
                Assert.assertEquals("Should not be called", true)
            }
        }
        val jsonObject = JSONObject("""{
            "label": "test-label"
        }""".trimIndent())
        val ninchatRadioButtonPresenter = NinchatRadioButtonPresenter(
                jsonObject = jsonObject,
                viewCallback = viewCallback
        )
        ninchatRadioButtonPresenter.renderCurrentView( isSelected = true, hasError = true)
    }

    @Test
    fun call_on_selected_callback_without_error() {
        val viewCallback = object : INinchatRadioButtonPresenter {
            override fun renderView(label: String, isSelected: Boolean, hasError: Boolean) {
                Assert.assertEquals("Should not be called", true)
            }

            override fun onSelected() {
                Assert.assertEquals(true, true)
            }

            override fun onUnSelected() {
                Assert.assertEquals("Should not be called", true)
            }

            override fun onError() {
                Assert.assertEquals("Should not be called", true)
            }
        }
        val jsonObject = JSONObject("""{
            "label": "test-label"
        }""".trimIndent())
        val ninchatRadioButtonPresenter = NinchatRadioButtonPresenter(
                jsonObject = jsonObject,
                viewCallback = viewCallback
        )
        ninchatRadioButtonPresenter.getNinchatRadioButtonModel().isSelected = false
        ninchatRadioButtonPresenter.getNinchatRadioButtonModel().hasError = false
        ninchatRadioButtonPresenter.onToggleSelection()
        Assert.assertEquals(false, ninchatRadioButtonPresenter.getNinchatRadioButtonModel().isSelected)
    }

    @Test
    fun call_on_un_selected_callback_without_error() {
        val viewCallback = object : INinchatRadioButtonPresenter {
            override fun renderView(label: String, isSelected: Boolean, hasError: Boolean) {
                Assert.assertEquals("Should not be called", true)
            }

            override fun onSelected() {
                Assert.assertEquals("Should not be called", true)
            }

            override fun onUnSelected() {
                Assert.assertEquals(true, true)
            }

            override fun onError() {
                Assert.assertEquals("Should not be called", true)
            }
        }
        val jsonObject = JSONObject("""{
            "label": "test-label"
        }""".trimIndent())
        val ninchatRadioButtonPresenter = NinchatRadioButtonPresenter(
                jsonObject = jsonObject,
                viewCallback = viewCallback
        )
        ninchatRadioButtonPresenter.getNinchatRadioButtonModel().isSelected = true
        ninchatRadioButtonPresenter.getNinchatRadioButtonModel().hasError = false
        ninchatRadioButtonPresenter.onToggleSelection()
        Assert.assertEquals(false, ninchatRadioButtonPresenter.getNinchatRadioButtonModel().isSelected)
    }

    @Test
    fun call_on_on_error() {
        val viewCallback = object : INinchatRadioButtonPresenter {
            override fun renderView(label: String, isSelected: Boolean, hasError: Boolean) {
                Assert.assertEquals("Should not be called", true)
            }

            override fun onSelected() {
                Assert.assertEquals(true, true)
            }

            override fun onUnSelected() {
                Assert.assertEquals("Should not be called", true)
            }

            override fun onError() {
                Assert.assertEquals(true, true)
            }
        }
        val jsonObject = JSONObject("""{
            "label": "test-label"
        }""".trimIndent())
        val ninchatRadioButtonPresenter = NinchatRadioButtonPresenter(
                jsonObject = jsonObject,
                viewCallback = viewCallback
        )
        ninchatRadioButtonPresenter.getNinchatRadioButtonModel().isSelected = false
        ninchatRadioButtonPresenter.getNinchatRadioButtonModel().hasError = true
        ninchatRadioButtonPresenter.onToggleSelection()
        Assert.assertEquals(true, ninchatRadioButtonPresenter.getNinchatRadioButtonModel().isSelected)
    }
}