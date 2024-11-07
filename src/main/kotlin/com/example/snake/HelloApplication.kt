package com.example.snake

import javafx.animation.AnimationTimer
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.input.KeyCode
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.stage.Popup
import javafx.stage.Stage
import java.util.Deque
import java.util.LinkedList
import kotlin.random.Random

class HelloApplication : Application() {

    private val map: ArrayList<ArrayList<Cell>> = ArrayList(20)
    private val snake: Deque<Pair<Int, Int>> = LinkedList()
    private val scores:ArrayList<Int> = ArrayList(20)
    private var dir = 0
    private var speed = 5

    private val cellSize = 30.0
    private val width = 20
    private val height = 20

    private var applePosition = 10 to 10
    private var skipPoll = false

    private var score = 0
    private var highScore = 0
    private var scoreNumber = Label("0")
    private var gameOverPopup = Popup()
    private var highScorePopup = Popup()
    private val highScoreText = Label("")
    private val startButton = Button(if(score == 0) "START" else "RESTART")
    private val scoreLabel = Label("SCORE:")
    private val pauseButton = Button("STOP")

    private var paused = false

    override fun start(stage: Stage) {
        val root = StackPane()
        configureUI(root)
        val scene = Scene(root, cellSize * width + 100, cellSize * height + 100)
        scene.setOnKeyPressed { event ->
            dir = when (event.code) {
                KeyCode.A -> {if(dir != 0) 2 else 0}
                KeyCode.W -> {if(dir != 1) 3 else 1}
                KeyCode.D -> {if(dir != 2) 0 else 2}
                KeyCode.S -> {if(dir != 3) 1 else 3}
                else -> {dir}
            }
        }
        stage.title = "Snake"
        stage.scene = scene
        stage.show()
    }

    private fun configureUI(root: StackPane){
        placeStartButton()
        placeScoreLabel()
        placeScoreText()
        placePauseButton()
        placeGameOverPopup()
        startButton.setOnAction{
            startButton.text = "RESTART"
            resetGame(root)
        }

        root.children.add(startButton)
        root.children.add(scoreLabel)
        root.children.add(scoreNumber)
        root.children.add(pauseButton)
    }

    private fun placeStartButton(){
        startButton.prefWidth = 100.0
        startButton.prefHeight = 40.0
        startButton.translateX = -width * cellSize / 2.0 + 40
        startButton.translateY = -height * cellSize / 2.0
    }

    private fun placeScoreLabel(){
        scoreLabel.prefWidth = 80.0
        scoreLabel.prefHeight = 40.0
        scoreLabel.translateX = width * cellSize / 4.0 - 120
        scoreLabel.translateY = -height * cellSize / 2.0 + 10
    }

    private fun placeScoreText(){
        scoreNumber.prefWidth = 40.0
        scoreNumber.prefHeight = 40.0
        scoreNumber.translateX = width * cellSize / 4.0 - 100
        scoreNumber.translateY = -height * cellSize / 2.0 + 10
        scoreNumber.text = "0"
    }

    private fun placePauseButton(){
        pauseButton.prefWidth = 100.0
        pauseButton.prefHeight = 40.0
        pauseButton.translateX = width * cellSize / 2.0 - 50
        pauseButton.translateY = -height * cellSize / 2.0
        pauseButton.isDisable = false
        pauseButton.setOnAction{
            if(paused){
                pauseButton.text = "STOP"
                animationTimer.start()
                paused = false
            }
            else{
                pauseButton.text = "START"
                animationTimer.stop()
                paused = true
            }
        }
    }

    private fun placeGameOverPopup(){
        gameOverPopup.width = 200.0
        gameOverPopup.height = 40.0
        val gameOverText = Label("GAME OVER")
        gameOverText.font = Font.font("Arial",FontWeight.BOLD, 20.0)
        gameOverText.textFill = Color.BLACK
        gameOverPopup.content.add(gameOverText)
        gameOverPopup.hide()
        gameOverPopup.anchorLocationProperty()
    }

    private fun placeHighScorePopup(){
        highScorePopup.width = 200.0
        highScorePopup.height = 40.0

        highScoreText.font = Font.font("Arial",FontWeight.BOLD, 20.0)
        highScoreText.textFill = Color.BLACK
        highScorePopup.anchorLocationProperty()
    }

    private fun resetGame(root: StackPane){
        highScorePopup.hide()
        root.children.clear()
        configureUI(root)
        buildTable(root)
        placeSnake()
        score = 0
        speed = 5
        highScore = 0
        dir = 3
        animationTimer.start()
    }

    private fun buildTable(root: StackPane){
        map.clear()
        for(x in 0 until width) {
            map.add(ArrayList())
            for(y in 0 until height){
                val cell = Cell(x - width / 2 , y - 8 , cellSize)
                map[x]+=cell
                root.children+=cell
            }
        }
    }

    private fun placeSnake(){
        snake.clear()
        snake.offer(width / 2 to height / 2)
        snake.offer(width / 2 to height / 2 - 4)
        snake.offer(width / 2 to height / 2 - 1)
    }

    private fun placeApple(){
        var eaten = false
        while(!eaten){
            val randX = Random.nextInt(0, width)
            val randY = Random.nextInt(0, height)
            if(!snake.contains(Pair(randX, randY))){
                eaten = true
                applePosition = randX to randY
            }
        }
    }

    private var animationTimer = object : AnimationTimer() {
        override fun handle(currentNanoTime: Long) {
            turnSnake()
            checkApple()
            checkTail()
            refreshScreen()
            try {
                Thread.sleep((1000.0 / speed).toLong())
            } catch (_: InterruptedException) {

            }
        }
    }

    fun turnSnake(){
        val head = snake.peekLast()
        var xDir = 0
        var yDir = 0
        when (dir){
            0 -> xDir = 1
            1 -> yDir = 1
            2 -> xDir = -1
            3 -> yDir = -1
        }
        if((head.first + xDir) !in 0 until width || (head.second + yDir) !in 0 until height){
            gameOver()
            return
        }
        snake.offer(Pair((head.first + xDir), (head.second + yDir)))
    }

    private fun checkApple() {
        if(snake.peekLast() == applePosition){
            skipPoll = true
            placeApple()
            score++
            speedUp()
            setHighScore(score)
            scoreNumber.text = "$score"
        }
    }
    private fun speedUp(){
        if(speed < 10){
            speed++
        }
    }

    private fun setHighScore(s:Int){
        scores.add(s)
        if(s > highScore){
            highScore = s
        }
    }
    fun checkTail(){
        for(segment in snake){
            if((snake.count{it == segment}) > 1){
                gameOver()
            }
        }
    }

    private fun gameOver(){
        placeHighScorePopup()
        animationTimer.stop()
        pauseButton.isDisable = true

        gameOverPopup.show(startButton.scene.window)
        if(highScore > 0) {
            gameOverPopup.hide()
            setUpHighScorePopUp()
        }
    }

    private fun setUpHighScorePopUp(){
        highScorePopup.content.clear()
        highScoreText.text = "HIGH SCORE: $highScore"
        highScorePopup.content.add(highScoreText)
        highScorePopup.show(startButton.scene.window)
    }

    fun refreshScreen() {
        Platform.runLater {
            if (skipPoll) {
                skipPoll = false
            } else {
                val cellCord = snake.poll()
                map[cellCord.first][cellCord.second].setColor("pink")
            }
            for (cord in snake) {
                val head: Pair<Int, Int> = snake.peekLast()
                if (cord == head) {
                    map[cord.first][cord.second].setColor("deepPink")
                } else {
                    map[cord.first][cord.second].setColor("deepSkyBlue")
                }
                map[applePosition.first][applePosition.second].setColor("green")
            }
        }
    }
}
fun main() {
    Application.launch(HelloApplication::class.java)
}