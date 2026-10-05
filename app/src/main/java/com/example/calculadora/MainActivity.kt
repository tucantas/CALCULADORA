package com.example.calculadora

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

class MainActivity : AppCompatActivity() {

    private lateinit var resultado: TextView
    private lateinit var switch: Switch

    private var entrada = ""            // número que se está escribiendo / mostrando
    private var acumulado: Double? = null // resultado parcial
    private var operacion: Char? = null   // operación pendiente (+, -, *, /)
    private var nuevaEntrada = false      // true -> el próximo dígito empieza un número nuevo

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        resultado = findViewById(R.id.resultado)
        switch = findViewById(R.id.switch1)

        // Dígitos 0-9
        val ids = listOf(
            R.id.button0, R.id.button1, R.id.button2, R.id.button3, R.id.button4,
            R.id.button5, R.id.button6, R.id.button7, R.id.button8, R.id.button9
        )
        ids.forEachIndexed { digito, id ->
            findViewById<Button>(id).setOnClickListener { pulsarDigito(digito.toString()) }
        }

        // Punto decimal (ajusta el ID si es otro)
        findViewById<Button>(R.id.btnpunto).setOnClickListener { pulsarPunto() }

        // Operaciones básicas
        findViewById<Button>(R.id.btnsuma).setOnClickListener { pulsarOperacion('+') }
        findViewById<Button>(R.id.btnresta).setOnClickListener { pulsarOperacion('-') }
        findViewById<Button>(R.id.btnPor).setOnClickListener { pulsarOperacion('*') }
        findViewById<Button>(R.id.btndiv).setOnClickListener { pulsarOperacion('/') }

        // Igual (ajusta el ID si es otro)
        findViewById<Button>(R.id.btnigual).setOnClickListener { pulsarIgual() }

        // Clear
        findViewById<Button>(R.id.btnclear).setOnClickListener { limpiar() }

        // Trigonometría
        findViewById<Button>(R.id.btnsin).setOnClickListener { trigonometria("sin") }
        findViewById<Button>(R.id.btncos).setOnClickListener { trigonometria("cos") }
        findViewById<Button>(R.id.btntang).setOnClickListener { trigonometria("tan") }

        // Switch: apagado = RAD, encendido = DEG
        switch.text = "RAD"
        switch.setOnCheckedChangeListener { _, isChecked ->
            switch.text = if (isChecked) "DEG" else "RAD"
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun pulsarDigito(d: String) {
        if (nuevaEntrada) {
            entrada = ""
            nuevaEntrada = false
        }
        entrada += d
        resultado.text = entrada
    }

    private fun pulsarPunto() {
        if (nuevaEntrada) {
            entrada = "0."
            nuevaEntrada = false
        } else if (entrada.isEmpty()) {
            entrada = "0."
        } else if (!entrada.contains('.')) {
            entrada += "."
        }
        resultado.text = entrada
    }

    private fun pulsarOperacion(op: Char) {
        val actual = entrada.toDoubleOrNull()
        if (actual != null) {
            val pendiente = operacion
            val previo = acumulado
            if (previo != null && pendiente != null && !nuevaEntrada) {
                val res = calcular(previo, actual, pendiente)
                if (res == null) { error(); return }
                acumulado = res
            } else {
                acumulado = actual
            }
            entrada = formatear(acumulado!!)
            resultado.text = entrada
        } else if (acumulado == null) {
            return
        }
        operacion = op
        nuevaEntrada = true
    }

    private fun pulsarIgual() {
        val actual = entrada.toDoubleOrNull()
        val previo = acumulado
        val pendiente = operacion
        if (actual == null || previo == null || pendiente == null || nuevaEntrada) return

        val res = calcular(previo, actual, pendiente)
        if (res == null) { error(); return }

        entrada = formatear(res)
        resultado.text = entrada
        acumulado = null
        operacion = null
        nuevaEntrada = true
    }

    private fun trigonometria(tipo: String) {
        val actual = entrada.toDoubleOrNull() ?: return
        val radianes = if (switch.isChecked) Math.toRadians(actual) else actual

        val res = when (tipo) {
            "sin" -> sin(radianes)
            "cos" -> cos(radianes)
            else -> tan(radianes)
        }

        // tan(90°) y similares dan valores enormes -> error
        if (res.isNaN() || res.isInfinite() || abs(res) > 1e12) { error(); return }

        entrada = formatear(res)
        resultado.text = entrada
        nuevaEntrada = true // la operación pendiente (si hay) se mantiene
    }

    private fun calcular(a: Double, b: Double, op: Char): Double? = when (op) {
        '+' -> a + b
        '-' -> a - b
        '*' -> a * b
        '/' -> if (b == 0.0) null else a / b
        else -> null
    }

    private fun limpiar() {
        entrada = ""
        acumulado = null
        operacion = null
        nuevaEntrada = false
        resultado.text = ""
    }

    private fun error() {
        limpiar()
        resultado.text = "Error"
    }

    // Redondea para evitar cosas como sin(180°) = 1.2246e-16 y quita el ".0" de los enteros
    private fun formatear(d: Double): String {
        val r = Math.round(d * 1e10) / 1e10
        return if (r == floor(r) && abs(r) < 1e15) r.toLong().toString() else r.toString()
    }
}