package com.kelompok.bouncingball;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Simulasi fisika murni agar mudah dijelaskan dan diuji tanpa Android. */
public class GameWorld {
    private static final int MAX_BODIES = 12;
    // Cukup kuat untuk membuat objek jatuh jelas sebelum menyentuh lantai.
    private static final float GRAVITY_Y = -4.0f;
    // Pantulan dinding hampir elastis agar energi tidak cepat habis.
    static final float RESTITUTION = 0.98f;
    // Batas kecepatan menjaga gerak tetap hidup tanpa melesat berlebihan.
    static final float MIN_SPEED = 2.0f;
    static final float MAX_SPEED = 3.5f;
    // Kecepatan samping kecil membuat awal permainan tampak jatuh lurus.
    private static final float MAX_START_SIDE_SPEED = 0.1f;
    // Lontaran vertikal minimum memastikan objek mencapai atap.
    private static final float MIN_LAUNCH_UPWARD_SPEED = 0.4f * MIN_SPEED;
    // Ambang menghindari normalisasi vektor yang panjangnya nyaris nol.
    private static final float MIN_DIRECTION_LENGTH_SQUARED = 0.000001f;
    // Kecepatan nol diberi arah baru, bukan dibiarkan berhenti.
    private static final float ZERO_SPEED_EPSILON = 0.000001f;
    // Nilai lama dipertahankan agar aturan tumbukan antar-objek tidak berubah.
    private static final float COLLISION_RESTITUTION = 0.94f;
    private final Random random;
    private final List<Body> bodies = new ArrayList<>();
    private GameStatus status;
    private int ballScore;
    private int pyramidScore;

    public GameWorld() {
        this(System.nanoTime());
    }

    public GameWorld(long seed) {
        random = new Random(seed);
        reset();
    }

    public void update(float dt) {
        if (status != GameStatus.PLAYING) {
            return;
        }
        for (Body body : bodies) {
            if (!body.active) {
                continue;
            }
            if (!body.hasLanded) {
                body.velocityY += GRAVITY_Y * dt;
            }
            body.x += body.velocityX * dt;
            body.y += body.velocityY * dt;
            body.z += body.velocityZ * dt;
            bounceAtWalls(body);
            keepMoving(body);
        }
        checkCollisions();
        if (!hasActiveBodies()) {
            status = GameStatus.FINISHED;
        }
    }

    public void start() {
        if (status != GameStatus.READY) {
            return;
        }
        status = GameStatus.PLAYING;
        for (Body body : bodies) {
            body.hasLanded = false;
            body.velocityX = randomBetween(-MAX_START_SIDE_SPEED, MAX_START_SIDE_SPEED);
            body.velocityY = 0f;
            body.velocityZ = randomBetween(-MAX_START_SIDE_SPEED, MAX_START_SIDE_SPEED);
        }
    }

    public void reset() {
        bodies.clear();
        bodies.add(new Body(-0.25f, 0f, 0f, 0.18f, Body.Type.BALL));
        bodies.add(new Body(0.25f, 0f, 0f, 0.18f, Body.Type.BALL));
        ballScore = 0;
        pyramidScore = 0;
        status = GameStatus.READY;
    }

    public boolean addPair(Body.Type type) {
        if (status != GameStatus.READY || bodies.size() + 2 > MAX_BODIES) {
            return false;
        }
        bodies.add(new Body(0f, 0f, 0f, 0.18f, type));
        bodies.add(new Body(0f, 0f, 0f, 0.18f, type));
        arrangeReadyBodies();
        return true;
    }

    public List<Body> getBodies() {
        return bodies;
    }

    public GameStatus getStatus() {
        return status;
    }

    public int getBallScore() {
        return ballScore;
    }

    public int getPyramidScore() {
        return pyramidScore;
    }

    private void bounceAtWalls(Body body) {
        float limit = 1f - body.radius;
        if (body.x < -limit) {
            body.x = -limit;
            body.velocityX = Math.abs(body.velocityX) * RESTITUTION;
        } else if (body.x > limit) {
            body.x = limit;
            body.velocityX = -Math.abs(body.velocityX) * RESTITUTION;
        }
        if (body.y < -limit) {
            body.y = -limit;
            if (!body.hasLanded) {
                body.hasLanded = true;
                launchAfterLanding(body);
            } else {
                body.velocityY = Math.abs(body.velocityY) * RESTITUTION;
            }
        } else if (body.y > limit) {
            body.y = limit;
            body.velocityY = -Math.abs(body.velocityY) * RESTITUTION;
        }
        if (body.z < -limit) {
            body.z = -limit;
            body.velocityZ = Math.abs(body.velocityZ) * RESTITUTION;
        } else if (body.z > limit) {
            body.z = limit;
            body.velocityZ = -Math.abs(body.velocityZ) * RESTITUTION;
        }
    }

    private void launchAfterLanding(Body body) {
        float[] direction;
        float speed;
        do {
            direction = randomUnitDirection();
            speed = randomBetween(MIN_SPEED, MAX_SPEED);
        } while (direction[1] * speed < MIN_LAUNCH_UPWARD_SPEED);
        body.velocityX = direction[0] * speed;
        body.velocityY = direction[1] * speed;
        body.velocityZ = direction[2] * speed;
    }

    private void keepMoving(Body body) {
        if (!body.active || !body.hasLanded) {
            return;
        }
        float speed = (float) Math.sqrt(body.velocityX * body.velocityX
                + body.velocityY * body.velocityY + body.velocityZ * body.velocityZ);
        if (speed < ZERO_SPEED_EPSILON) {
            float[] direction = randomUnitDirection();
            body.velocityX = direction[0] * MIN_SPEED;
            body.velocityY = direction[1] * MIN_SPEED;
            body.velocityZ = direction[2] * MIN_SPEED;
        } else if (speed < MIN_SPEED || speed > MAX_SPEED) {
            float targetSpeed = Math.max(MIN_SPEED, Math.min(MAX_SPEED, speed));
            float scale = targetSpeed / speed;
            body.velocityX *= scale;
            body.velocityY *= scale;
            body.velocityZ *= scale;
        }
    }

    private float[] randomUnitDirection() {
        float x;
        float y;
        float z;
        float lengthSquared;
        do {
            x = randomBetween(-1f, 1f);
            y = randomBetween(-1f, 1f);
            z = randomBetween(-1f, 1f);
            lengthSquared = x * x + y * y + z * z;
        } while (lengthSquared <= MIN_DIRECTION_LENGTH_SQUARED || lengthSquared > 1f);
        float inverseLength = 1f / (float) Math.sqrt(lengthSquared);
        return new float[]{x * inverseLength, y * inverseLength, z * inverseLength};
    }

    private float randomBetween(float minimum, float maximum) {
        return minimum + random.nextFloat() * (maximum - minimum);
    }

    private void checkCollisions() {
        for (int firstIndex = 0; firstIndex < bodies.size(); firstIndex++) {
            Body first = bodies.get(firstIndex);
            if (!first.active) {
                continue;
            }
            for (int secondIndex = firstIndex + 1; secondIndex < bodies.size(); secondIndex++) {
                Body second = bodies.get(secondIndex);
                if (!second.active) {
                    continue;
                }
                float dx = second.x - first.x;
                float dy = second.y - first.y;
                float dz = second.z - first.z;
                float radiusSum = first.radius + second.radius;
                float distanceSquared = dx * dx + dy * dy + dz * dz;
                if (distanceSquared >= radiusSum * radiusSum) {
                    continue;
                }
                if (first.type == second.type) {
                    // Pasangan dengan bentuk sama langsung hilang sebagai satu skor.
                    first.active = false;
                    second.active = false;
                    if (first.type == Body.Type.BALL) {
                        ballScore++;
                    } else {
                        pyramidScore++;
                    }
                    break;
                } else {
                    bounceApart(first, second, dx, dy, dz, distanceSquared, radiusSum);
                    // Koreksi posisi tumbukan bisa mendekatkan objek ke sisi wadah.
                    bounceAtWalls(first);
                    bounceAtWalls(second);
                    keepMoving(first);
                    keepMoving(second);
                }
            }
        }
    }

    private void bounceApart(Body first, Body second, float dx, float dy, float dz,
                             float distanceSquared, float radiusSum) {
        float distance = (float) Math.sqrt(distanceSquared);
        float normalX = distance > 0f ? dx / distance : 1f;
        float normalY = distance > 0f ? dy / distance : 0f;
        float normalZ = distance > 0f ? dz / distance : 0f;
        float inverseFirstMass = 1f / first.mass;
        float inverseSecondMass = 1f / second.mass;
        float inverseMassSum = inverseFirstMass + inverseSecondMass;

        float relativeSpeed = (second.velocityX - first.velocityX) * normalX
                + (second.velocityY - first.velocityY) * normalY
                + (second.velocityZ - first.velocityZ) * normalZ;
        if (relativeSpeed < 0f) {
            float impulse = -(1f + COLLISION_RESTITUTION) * relativeSpeed / inverseMassSum;
            first.velocityX -= impulse * inverseFirstMass * normalX;
            first.velocityY -= impulse * inverseFirstMass * normalY;
            first.velocityZ -= impulse * inverseFirstMass * normalZ;
            second.velocityX += impulse * inverseSecondMass * normalX;
            second.velocityY += impulse * inverseSecondMass * normalY;
            second.velocityZ += impulse * inverseSecondMass * normalZ;
        }

        float overlap = radiusSum - distance;
        float correction = Math.max(overlap - 0.001f, 0f) * 0.8f / inverseMassSum;
        first.x -= correction * inverseFirstMass * normalX;
        first.y -= correction * inverseFirstMass * normalY;
        first.z -= correction * inverseFirstMass * normalZ;
        second.x += correction * inverseSecondMass * normalX;
        second.y += correction * inverseSecondMass * normalY;
        second.z += correction * inverseSecondMass * normalZ;
    }

    private boolean hasActiveBodies() {
        for (Body body : bodies) {
            if (body.active) {
                return true;
            }
        }
        return false;
    }

    private void arrangeReadyBodies() {
        int columns = 3;
        int rows = (bodies.size() + columns - 1) / columns;
        for (int index = 0; index < bodies.size(); index++) {
            int row = index / columns;
            int column = index % columns;
            int itemsInRow = Math.min(columns, bodies.size() - row * columns);
            Body body = bodies.get(index);
            body.x = (column - (itemsInRow - 1) / 2f) * 0.42f;
            body.y = 0f;
            body.z = (row - (rows - 1) / 2f) * 0.5f;
        }
    }
}
