package com.example.snake

import javafx.scene.control.Button
import javafx.scene.transform.Translate

class Cell(x: Int, y: Int, size: Double): Button() {

    init {
        translateX = size * x
        translateY = size * y
        prefWidth = size
        prefHeight = size
        isDisable = true
        style = """
            -fx-background-color: white;
            -fx-background-radius: 0px;
            -fx-focus-color: transparent;
            -fx-width: 20px;
            -fx-height: 20px;
            -fx-focus-traversable: false;
            -fx-faint-focus: false;
            -fx-border-width: 1px 1px 1px 1px;
            -fx-border-color: black;
        """.trimIndent()
    }

    fun setColor(color: String){
        style = """
            -fx-background-color: $color;
            -fx-background-radius: 0px;
            -fx-focus-color: transparent;
            -fx-width: 20px;
            -fx-height: 20px;
            -fx-focus-traversable: false;
            -fx-faint-focus: false;
            -fx-border-width: 1px 1px 1px 1px;
            -fx-border-color: black;
        """.trimIndent()
    }
}