package com.ninchat.sdk.utils.propsvisitor

import com.ninchat.client.Objects
import com.ninchat.client.PropVisitor
import com.ninchat.client.Props
import com.ninchat.client.Strings

class NinchatPropVisitor : PropVisitor {
    @JvmField
    val properties = mutableMapOf<String, Any>()

    @Throws(Exception::class)
    override fun visitBool(p0: String, p1: Boolean) {
        properties[p0] = p1
    }

    @Throws(Exception::class)
    override fun visitNumber(p0: String, p1: Double) {
        properties[p0] = p1
    }

    @Throws(Exception::class)
    override fun visitObject(p0: String, p1: Props) {
        properties[p0] = p1
    }

    @Throws(Exception::class)
    override fun visitString(p0: String, p1: String) {
        properties[p0] = p1
    }

    @Throws(Exception::class)
    override fun visitStringArray(p0: String, p1: Strings) {
        properties[p0] = p1
    }

    @Throws(Exception::class)
    override fun visitObjectArray(p0: String, p1: Objects) {
        properties[p0] = p1
    }
}