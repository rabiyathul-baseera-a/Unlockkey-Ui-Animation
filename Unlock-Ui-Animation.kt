package skynetbee.rabiya.myapplication

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

private val TWO_PI = (2.0 * PI).toFloat()
private fun rnd() = Random.nextFloat()

// thin lines of the starburst in the centre
private class Spike(val angle: Float, val len: Float, val width: Float)

// long glass needles shooting out
private class Needle(
    val angle: Float, val speed: Float, val length: Float,
    val width: Float, val depth: Float, val colors: List<Color>
)

// big faceted rainbow glass pieces
private class Chunk(
    val angle: Float, val speed: Float, val size: Float, val spin: Float,
    val depth: Float, val shape: List<Offset>, val colors: List<Color>
)

// tiny sparkling debris
private class Spark(val angle: Float, val speed: Float, val size: Float, val color: Color, val seed: Float)

// background stars
private class Star(val fx: Float, val fy: Float, val r: Float, val a: Float)

@Composable
fun CrystalLockScreen() {
    val scope = rememberCoroutineScope()
    val keyIn = remember { Animatable(0f) }     // key flying in
    val keyTurn = remember { Animatable(0f) }   // key twisting
    val shackle = remember { Animatable(0f) }   // lock opening
    val blast = remember { Animatable(0f) }     // crystal blast
    var busy by remember { mutableStateOf(false) }
    var unlocked by remember { mutableStateOf(false) }

    val infinite = rememberInfiniteTransition(label = "idle")
    val pulse by infinite.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    // rainbow glass colour sets
    val palettes = remember {
        listOf(
            listOf(Color(0xFF7C4DFF), Color(0xFF40C4FF), Color(0xFFFFFFFF)),
            listOf(Color(0xFFFF80AB), Color(0xFFB388FF), Color(0xFF18FFFF)),
            listOf(Color(0xFFFFD180), Color(0xFFFF80AB), Color(0xFF7C4DFF)),
            listOf(Color(0xFF00E5FF), Color(0xFF304FFE), Color(0xFFE040FB))
        )
    }
    val sparkColors = remember {
        listOf(
            Color(0xFFFFFFFF), Color(0xFFB388FF), Color(0xFF80D8FF),
            Color(0xFFFF80AB), Color(0xFFFFD180)
        )
    }

    val spikes = remember {
        List(110) { Spike(rnd() * TWO_PI, 1.2f + rnd().pow(2f) * 3.5f, 0.02f + rnd() * 0.04f) }
    }
    val needles = remember {
        List(46) {
            Needle(
                angle = rnd() * TWO_PI,
                speed = 1.5f + rnd() * 9f,
                length = 1.2f + rnd() * 3.2f,
                width = 0.06f + rnd() * 0.16f,
                depth = rnd(),
                colors = palettes.random()
            )
        }
    }
    val chunks = remember {
        List(26) {
            val n = 4 + Random.nextInt(2)
            val shape = List(n) { i ->
                val a = (i + rnd() * 0.6f) / n * TWO_PI
                val rr = 0.55f + rnd() * 0.45f
                Offset(cos(a) * rr, sin(a) * rr)
            }
            Chunk(
                angle = rnd() * TWO_PI,
                speed = 2f + rnd() * 9f,
                size = 0.25f + rnd() * 0.55f,
                spin = (rnd() * 2f - 1f) * 540f,
                depth = rnd(),
                shape = shape,
                colors = palettes.random()
            )
        }
    }
    val sparks = remember {
        List(260) {
            Spark(
                angle = rnd() * TWO_PI,
                speed = 0.5f + rnd().pow(1.3f) * 12f,
                size = 0.02f + rnd() * 0.06f,
                color = sparkColors.random(),
                seed = rnd() * TWO_PI
            )
        }
    }
    val stars = remember {
        List(70) { Star(rnd(), rnd(), 0.6f + rnd() * 1.6f, 0.2f + rnd() * 0.6f) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF05030F), Color(0xFF1A0B3D))))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (busy) return@clickable
                busy = true
                scope.launch {
                    if (!unlocked) {
                        keyIn.animateTo(1f, tween(750, easing = FastOutSlowInEasing))
                        keyTurn.animateTo(1f, tween(500))
                        shackle.animateTo(1f, tween(350))
                        delay(150)
                        blast.animateTo(1f, tween(2000, easing = LinearEasing))
                        unlocked = true
                    } else {
                        blast.snapTo(0f)
                        shackle.snapTo(0f)
                        keyIn.snapTo(0f)
                        keyTurn.snapTo(0f)
                        unlocked = false
                    }
                    busy = false
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val u = size.minDimension / 10f      // 1 unit
            val c = center
            val b = blast.value
            val e = 1f - (1f - b) * (1f - b) * (1f - b)   // fast start, slow end

            val bodyW = 4f * u
            val bodyH = 3f * u
            val bodyLeft = c.x - bodyW / 2f
            val bodyTop = c.y - bodyH / 2f + 0.8f * u
            val bodyCenter = Offset(c.x, bodyTop + bodyH / 2f)

            // ---------- background stars ----------
            stars.forEach { s ->
                drawCircle(
                    color = Color.White.copy(alpha = (s.a * (0.6f + 0.4f * pulse)).coerceIn(0f, 1f)),
                    radius = s.r * 1.5f,
                    center = Offset(s.fx * size.width, s.fy * size.height)
                )
            }

            // ---------- LOCK + KEY (before blast) ----------
            if (b == 0f) {
                // idle purple aura
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0xFF7C4DFF).copy(alpha = pulse * 0.5f), Color.Transparent),
                        center = bodyCenter,
                        radius = 5.5f * u
                    ),
                    radius = 5.5f * u,
                    center = bodyCenter
                )

                val open = shackle.value
                val legX1 = c.x - 1.2f * u
                val legX2 = c.x + 1.2f * u
                val legBottom = bodyTop
                val legTop = bodyTop - 1.2f * u

                translate(top = -0.7f * u * open) {
                    rotate(degrees = -25f * open, pivot = Offset(legX2, legBottom)) {
                        val stroke = Stroke(width = 0.55f * u, cap = StrokeCap.Round)
                        val metal = Color(0xFFD1C4E9)
                        drawLine(metal, Offset(legX1, legTop), Offset(legX1, legBottom), 0.55f * u, StrokeCap.Round)
                        drawLine(metal, Offset(legX2, legTop), Offset(legX2, legBottom), 0.55f * u, StrokeCap.Round)
                        drawArc(
                            color = metal,
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(legX1, legTop - 1.2f * u),
                            size = Size(2.4f * u, 2.4f * u),
                            style = stroke
                        )
                    }
                }

                // lock body
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF9575FF), Color(0xFF3F4FD9)),
                        startY = bodyTop,
                        endY = bodyTop + bodyH
                    ),
                    topLeft = Offset(bodyLeft, bodyTop),
                    size = Size(bodyW, bodyH),
                    cornerRadius = CornerRadius(0.4f * u)
                )
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.6f),
                    topLeft = Offset(bodyLeft, bodyTop),
                    size = Size(bodyW, bodyH),
                    cornerRadius = CornerRadius(0.4f * u),
                    style = Stroke(width = 0.08f * u)
                )
                // keyhole
                drawCircle(Color(0xFF05030F), radius = 0.4f * u, center = bodyCenter.copy(y = bodyCenter.y - 0.2f * u))
                drawRoundRect(
                    color = Color(0xFF05030F),
                    topLeft = Offset(bodyCenter.x - 0.15f * u, bodyCenter.y - 0.1f * u),
                    size = Size(0.3f * u, 0.9f * u),
                    cornerRadius = CornerRadius(0.1f * u)
                )

                // ---------- KEY ----------

                // ---------- GOLDEN 3D KEY ----------
                val ki = keyIn.value
                val kt = keyTurn.value

                if (ki > 0f) {
                    val tip = Offset(
                        bodyCenter.x,
                        bodyCenter.y - 0.2f * u
                    )

                    val ka = ki.coerceIn(0f, 1f)
                    val gold = Color(0xFFFFD54F).copy(alpha = ka)
                    val goldLight = Color(0xFFFFF3B0).copy(alpha = ka)
                    val goldMid = Color(0xFFFFB300).copy(alpha = ka)
                    val goldDark = Color(0xFF9A5700).copy(alpha = ka)

                    // Soft golden glow around the keyhole
                    if (kt > 0f) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFFD54F).copy(
                                        alpha = 0.65f * kt
                                    ),
                                    Color(0xFFFFA000).copy(
                                        alpha = 0.25f * kt
                                    ),
                                    Color.Transparent
                                ),
                                center = tip,
                                radius = 1.8f * u
                            ),
                            radius = 1.8f * u,
                            center = tip
                        )
                    }

                    // Fly into position, then rotate to unlock
                    translate(
                        left = (1f - ki) * 2.5f * u,
                        top = (1f - ki) * 6f * u
                    ) {
                        rotate(
                            degrees = -25f * (1f - ki) + 35f * kt,
                            pivot = tip
                        ) {
                            // Three-quarter perspective:
                            // small depth offset creates a raised 3D edge
                            val depthX = 0.12f * u
                            val depthY = 0.10f * u

                            val shaftW = 0.32f * u
                            val shaftLen = 2.2f * u

                            val shaftTop = tip.y
                            val shaftLeft = tip.x - shaftW / 2f

                            // 1. Dark extruded edge
                            drawRoundRect(
                                color = goldDark,
                                topLeft = Offset(
                                    shaftLeft + depthX,
                                    shaftTop + depthY
                                ),
                                size = Size(
                                    shaftW,
                                    shaftLen
                                ),
                                cornerRadius = CornerRadius(
                                    shaftW / 2f
                                )
                            )

                            // 2. Main metallic shaft
                            drawRoundRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        goldDark,
                                        goldMid,
                                        goldLight,
                                        gold,
                                        goldDark
                                    ),
                                    startX = shaftLeft,
                                    endX = shaftLeft + shaftW
                                ),
                                topLeft = Offset(
                                    shaftLeft,
                                    shaftTop
                                ),
                                size = Size(
                                    shaftW,
                                    shaftLen
                                ),
                                cornerRadius = CornerRadius(
                                    shaftW / 2f
                                )
                            )

                            // 3. Bright reflective strip
                            drawRoundRect(
                                color = goldLight,
                                topLeft = Offset(
                                    shaftLeft + shaftW * 0.20f,
                                    shaftTop + 0.12f * u
                                ),
                                size = Size(
                                    shaftW * 0.16f,
                                    shaftLen * 0.70f
                                ),
                                cornerRadius = CornerRadius(
                                    shaftW * 0.08f
                                )
                            )

                            // 4. First raised key tooth
                            drawRoundRect(
                                color = goldDark,
                                topLeft = Offset(
                                    shaftLeft + shaftW * 0.72f + depthX,
                                    shaftTop + 0.35f * u + depthY
                                ),
                                size = Size(
                                    0.38f * u,
                                    0.23f * u
                                ),
                                cornerRadius = CornerRadius(
                                    0.05f * u
                                )
                            )

                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        goldLight,
                                        goldMid,
                                        gold
                                    )
                                ),
                                topLeft = Offset(
                                    shaftLeft + shaftW * 0.62f,
                                    shaftTop + 0.35f * u
                                ),
                                size = Size(
                                    0.38f * u,
                                    0.23f * u
                                ),
                                cornerRadius = CornerRadius(
                                    0.05f * u
                                )
                            )

                            // 5. Second raised key tooth
                            drawRoundRect(
                                color = goldDark,
                                topLeft = Offset(
                                    shaftLeft + shaftW * 0.72f + depthX,
                                    shaftTop + 0.78f * u + depthY
                                ),
                                size = Size(
                                    0.30f * u,
                                    0.22f * u
                                ),
                                cornerRadius = CornerRadius(
                                    0.05f * u
                                )
                            )

                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        goldLight,
                                        goldMid
                                    )
                                ),
                                topLeft = Offset(
                                    shaftLeft + shaftW * 0.62f,
                                    shaftTop + 0.78f * u
                                ),
                                size = Size(
                                    0.30f * u,
                                    0.22f * u
                                ),
                                cornerRadius = CornerRadius(
                                    0.05f * u
                                )
                            )

                            // 6. Three-quarter perspective key ring
                            val ringCenter = Offset(
                                tip.x,
                                tip.y + shaftLen + 0.48f * u
                            )

                            // Extruded shadow of the ring
                            drawOval(
                                color = goldDark,
                                topLeft = Offset(
                                    ringCenter.x - 0.60f * u + depthX,
                                    ringCenter.y - 0.57f * u + depthY
                                ),
                                size = Size(
                                    1.20f * u,
                                    1.14f * u
                                ),
                                style = Stroke(
                                    width = 0.32f * u
                                )
                            )

                            // Main ring with metallic gradient
                            drawOval(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        goldLight,
                                        goldMid,
                                        gold,
                                        goldDark,
                                        goldLight
                                    ),
                                    start = Offset(
                                        ringCenter.x - 0.7f * u,
                                        ringCenter.y - 0.7f * u
                                    ),
                                    end = Offset(
                                        ringCenter.x + 0.7f * u,
                                        ringCenter.y + 0.7f * u
                                    )
                                ),
                                topLeft = Offset(
                                    ringCenter.x - 0.60f * u,
                                    ringCenter.y - 0.57f * u
                                ),
                                size = Size(
                                    1.20f * u,
                                    1.14f * u
                                ),
                                style = Stroke(
                                    width = 0.30f * u
                                )
                            )

                            // Inner dark opening gives the ring depth
                            drawOval(
                                color = Color(0xFF170D27).copy(
                                    alpha = 0.85f * ka
                                ),
                                topLeft = Offset(
                                    ringCenter.x - 0.39f * u,
                                    ringCenter.y - 0.36f * u
                                ),
                                size = Size(
                                    0.78f * u,
                                    0.72f * u
                                )
                            )

                            // Small specular shine on the ring
                            drawArc(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        goldLight,
                                        Color.White.copy(alpha = ka),
                                        gold
                                    )
                                ),
                                startAngle = 205f,
                                sweepAngle = 95f,
                                useCenter = false,
                                topLeft = Offset(
                                    ringCenter.x - 0.60f * u,
                                    ringCenter.y - 0.57f * u
                                ),
                                size = Size(
                                    1.20f * u,
                                    1.14f * u
                                ),
                                style = Stroke(
                                    width = 0.10f * u
                                )
                            )
                        }
                    }
                }
            }

            // ---------- CRYSTAL BLAST ----------
            if (b > 0f) {
                val fade = (1f - b * b).coerceIn(0f, 1f)

                // white screen flash at the start
                val flash = (1f - b * 6f).coerceIn(0f, 1f)
                if (flash > 0f) {
                    drawRect(Color.White.copy(alpha = flash * 0.55f))
                }

                // purple-white glowing core
                val coreA = (1f - b * 1.3f).coerceIn(0f, 1f)
                val coreR = 3.5f * u + 5f * u * e
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(
                            Color.White.copy(alpha = coreA),
                            Color(0xFFB388FF).copy(alpha = coreA * 0.7f),
                            Color(0xFF536DFE).copy(alpha = coreA * 0.25f),
                            Color.Transparent
                        ),
                        center = bodyCenter,
                        radius = coreR
                    ),
                    radius = coreR,
                    center = bodyCenter
                )

                // shock ring
                drawCircle(
                    color = Color(0xFFD1C4E9).copy(alpha = ((1f - e) * 0.8f).coerceIn(0f, 1f)),
                    radius = 0.5f * u + 10f * u * e,
                    center = bodyCenter,
                    style = Stroke(width = 0.12f * u)
                )

                // starburst spikes in the centre
                spikes.forEach { s ->
                    val ca = cos(s.angle)
                    val sa = sin(s.angle)
                    val r0 = 0.2f * u + s.len * 0.2f * u * e
                    val r1 = s.len * u * (0.4f + 1.6f * e)
                    drawLine(
                        color = Color(0xFFE1D8FF).copy(alpha = fade * 0.9f),
                        start = Offset(bodyCenter.x + ca * r0, bodyCenter.y + sa * r0),
                        end = Offset(bodyCenter.x + ca * r1, bodyCenter.y + sa * r1),
                        strokeWidth = s.width * u
                    )
                }

                // long glass needles with rainbow colours
                needles.forEach { n ->
                    val ca = cos(n.angle)
                    val sa = sin(n.angle)
                    val tail = n.speed * 0.6f * u * e
                    val head = (n.speed * e + n.length * (0.5f + e)) * u
                    val w = n.width * u * (1f + n.depth * e * 1.5f)
                    val tp = Offset(bodyCenter.x + ca * tail, bodyCenter.y + sa * tail)
                    val hp = Offset(bodyCenter.x + ca * head, bodyCenter.y + sa * head)
                    val mid = tail + (head - tail) * 0.35f
                    val mx = bodyCenter.x + ca * mid
                    val my = bodyCenter.y + sa * mid
                    val px = -sa * w
                    val py = ca * w
                    val path = Path().apply {
                        moveTo(tp.x, tp.y)
                        lineTo(mx + px, my + py)
                        lineTo(hp.x, hp.y)
                        lineTo(mx - px, my - py)
                        close()
                    }
                    drawPath(path, Brush.linearGradient(n.colors, start = tp, end = hp), alpha = fade)
                }

                // big faceted glass chunks tumbling
                chunks.forEach { ch ->
                    val d = ch.speed * u * e
                    val x = bodyCenter.x + cos(ch.angle) * d
                    val y = bodyCenter.y + sin(ch.angle) * d + 1.5f * u * e * e
                    val r = ch.size * u * (1f + ch.depth * 2f * e)
                    val path = Path().apply {
                        ch.shape.forEachIndexed { i, v ->
                            val px = x + v.x * r
                            val py = y + v.y * r
                            if (i == 0) moveTo(px, py) else lineTo(px, py)
                        }
                        close()
                    }
                    rotate(degrees = ch.spin * e, pivot = Offset(x, y)) {
                        drawPath(
                            path,
                            Brush.linearGradient(ch.colors, start = Offset(x - r, y - r), end = Offset(x + r, y + r)),
                            alpha = fade
                        )
                        drawPath(path, Color.White.copy(alpha = fade * 0.8f), style = Stroke(width = 0.04f * u))
                    }
                }

                // tiny twinkling sparks
                sparks.forEach { sp ->
                    val d = sp.speed * u * e
                    val x = bodyCenter.x + cos(sp.angle) * d
                    val y = bodyCenter.y + sin(sp.angle) * d + 1.0f * u * e * e
                    val twinkle = 0.5f + 0.5f * sin(b * 40f + sp.seed)
                    drawCircle(
                        color = sp.color.copy(alpha = (fade * twinkle).coerceIn(0f, 1f)),
                        radius = sp.size * u,
                        center = Offset(x, y)
                    )
                }
            }
        }

        Text(
            text = when {
                unlocked -> "Unlocked! 🔓 (tap to reset)"
                busy -> "Unlocking..."
                else -> "Tap the lock"
            },
            color = Color.White,
            fontSize = 20.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 96.dp)
        )
    }
}