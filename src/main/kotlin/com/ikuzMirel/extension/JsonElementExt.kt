package com.ikuzMirel.extension

import kotlinx.serialization.json.JsonElement

fun JsonElement?.removeQuotes(): String {
    return this.toString().removeSurrounding("\"")
}