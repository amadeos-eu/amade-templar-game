package eu.amadeos.games.templar.ui.art;

/**
 * Where the limbs of a person are, as angles seen from the side, in radians: 0 hangs straight down, more
 * swings forward, towards where the person looks. {@link Figure} reads the same pose from the front and from
 * behind, so one pose serves all three views.
 *
 * @param lean    how far the upper body leans forward
 * @param crouch  how far the hips sink, in pixels at height 118 (for kneeling and picking things up)
 * @param spread  how far the hands are held out to the sides, 0 to 1 (a shrug)
 * @param mouth   0 closed, 1 open
 */
public record Pose(double lean, double crouch, double nearHip, double nearKnee, double farHip, double farKnee,
                   double nearShoulder, double nearElbow, double farShoulder, double farElbow, double spread,
                   double mouth, double headTilt) {

    /** Standing at ease. */
    public static final Pose STAND = new Pose(0, 0, 0.03, 0.06, -0.03, 0.04, 0.04, 0.14, -0.03, 0.12, 0, 0, 0);

    /** The same pose with the mouth open or closed. */
    public Pose talking(boolean open) {
        return new Pose(lean, crouch, nearHip, nearKnee, farHip, farKnee, nearShoulder, nearElbow, farShoulder,
                farElbow, spread, open ? 1 : 0, headTilt + (open ? -0.03 : 0));
    }

    /**
     * One moment of a walk: {@code phase} 0 to 1 is a whole cycle of two steps. Each leg swings forward with
     * its knee bent and comes down nearly straight; the arms swing against the legs.
     */
    // --8<-- [start:walk]
    public static Pose walk(double phase) {
        double near = phase * Math.PI * 2, far = near + Math.PI;
        return new Pose(0.05, 0, thigh(near), knee(near), thigh(far), knee(far),
                -0.34 * Math.sin(near), elbow(-Math.sin(near)), -0.34 * Math.sin(far), elbow(-Math.sin(far)),
                0, 0, 0);
    }

    private static double thigh(double t) {
        return 0.4 * Math.sin(t);
    }

    private static double knee(double t) {
        double swing = Math.max(0, Math.cos(t + 0.45));
        return 0.07 + 0.8 * swing * swing;
    }

    private static double elbow(double forward) {
        return 0.16 + 0.32 * Math.max(0, forward);
    }
    // --8<-- [end:walk]

    /**
     * The pose of a named animation at {@code t}, 0 to 1 through it: {@code reach} bends over a table,
     * {@code pickup} goes down for something on the floor, {@code point}, {@code shrug}, {@code phone} at the
     * ear, {@code startle}, {@code kneel}, {@code flee}.
     */
    public static Pose of(String animation, double t) {
        double in = ease(Math.min(1, t * 3.5)), out = ease(Math.min(1, (1 - t) * 3.5));
        double k = Math.min(in, out);
        return switch (animation) {
            case "reach" -> blend(STAND, new Pose(0.4, 2, 0.1, 0.14, -0.04, 0.06, 0.62, 0.3, 0.5, 0.4, 0, 0, 0.25), k);
            case "pickup" -> blend(STAND, new Pose(0.6, 24, 1.25, 2.1, 0.55, 2.05, 0.45, 0.2, 0.35, 0.25, 0, 0, 0.3), k);
            case "kneel" -> blend(STAND, new Pose(0.25, 26, 1.45, 1.7, -0.4, 1.8, 0.8, 0.5, 0.6, 0.6, 0, 0, 0.15), k);
            case "point" -> blend(STAND, new Pose(0.0, 0, 0.03, 0.06, -0.03, 0.04, 1.75, 0.05, -0.05, 0.12, 0, 0, -0.1), k);
            case "shrug" -> blend(STAND, new Pose(-0.04, 0, 0.03, 0.06, -0.03, 0.04, 0.35, 1.6, 0.35, 1.6, 1, 0, 0.12), k);
            case "phone" -> blend(STAND, new Pose(0.0, 0, 0.03, 0.06, -0.03, 0.04, 1.2, 2.5, -0.03, 0.12, 0, 0, 0.1), k);
            case "startle" -> blend(STAND, new Pose(-0.18, 0, -0.1, 0.1, -0.2, 0.15, 0.9, 1.9, 0.7, 1.9, 0.6, 1, -0.2), k);
            case "flee" -> walk(t * 2.5);
            default -> STAND;
        };
    }

    private static double ease(double t) {
        return t * t * (3 - 2 * t);
    }

    static Pose blend(Pose a, Pose b, double t) {
        return new Pose(l(a.lean, b.lean, t), l(a.crouch, b.crouch, t), l(a.nearHip, b.nearHip, t),
                l(a.nearKnee, b.nearKnee, t), l(a.farHip, b.farHip, t), l(a.farKnee, b.farKnee, t),
                l(a.nearShoulder, b.nearShoulder, t), l(a.nearElbow, b.nearElbow, t),
                l(a.farShoulder, b.farShoulder, t), l(a.farElbow, b.farElbow, t), l(a.spread, b.spread, t),
                l(a.mouth, b.mouth, t), l(a.headTilt, b.headTilt, t));
    }

    private static double l(double a, double b, double t) {
        return a + (b - a) * t;
    }
}
