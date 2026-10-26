package eu.amadeos.games.templar.ui.game;

import eu.amadeos.gameengine.audio.Sound;
import eu.amadeos.gameengine.audio.Sound.Wave;
import eu.amadeos.gameengine.audio.Tune;

import java.util.Map;

/** The sounds the scripts ask for by name, made once from tones and noise: no recordings. */
final class SoundBank {
    private final Map<String, Sound> sounds = Map.ofEntries(
            Map.entry("paper", Sound.tone(Wave.NOISE, 0, 0, 0.18, 0.05).then(Sound.tone(Wave.NOISE, 0, 0, 0.1, 0.03))),
            Map.entry("coin", Sound.tone(Wave.SQUARE, 2093, 2093, 0.05, 0.04).then(Sound.tone(Wave.SQUARE, 2637, 2637, 0.16, 0.035))),
            Map.entry("coffee", Sound.mix(Sound.tone(Wave.NOISE, 0, 0, 1.4, 0.05), Sound.tone(Wave.TRIANGLE, 90, 70, 1.4, 0.12))),
            Map.entry("door", Sound.mix(Sound.tone(Wave.TRIANGLE, 110, 60, 0.3, 0.3), Sound.tone(Wave.NOISE, 0, 0, 0.08, 0.06))),
            Map.entry("creak", Sound.tone(Wave.SQUARE, 180, 240, 0.5, 0.025).then(Sound.tone(Wave.SQUARE, 240, 160, 0.4, 0.02))),
            Map.entry("cat", Sound.tone(Wave.TRIANGLE, 520, 880, 0.18, 0.15).then(Sound.tone(Wave.TRIANGLE, 880, 480, 0.35, 0.12))),
            Map.entry("hiss", Sound.tone(Wave.NOISE, 0, 0, 0.5, 0.08)),
            Map.entry("pigeons", Sound.tone(Wave.NOISE, 0, 0, 0.06, 0.08).then(Sound.tone(Wave.NOISE, 0, 0, 0.05, 0.02))
                    .then(Sound.tone(Wave.NOISE, 0, 0, 0.06, 0.08)).then(Sound.tone(Wave.NOISE, 0, 0, 0.05, 0.02))
                    .then(Sound.tone(Wave.NOISE, 0, 0, 0.07, 0.07))),
            Map.entry("coo", Sound.tone(Wave.TRIANGLE, 330, 300, 0.25, 0.14).then(Sound.tone(Wave.TRIANGLE, 300, 260, 0.35, 0.12))),
            Map.entry("bells", Tune.voice(Wave.TRIANGLE, 0.22, "t70 o4 l4 g e c g")),
            Map.entry("alarm", Tune.voice(Wave.SQUARE, 0.06, "t200 o3 l8 c c c r c c c")),
            Map.entry("seal", Sound.mix(Sound.tone(Wave.TRIANGLE, 160, 90, 0.25, 0.3), Sound.tone(Wave.NOISE, 0, 0, 0.05, 0.05))),
            Map.entry("discovery", Tune.voice(Wave.TRIANGLE, 0.14, "t90 o4 l8 a >c e <b- l2 a")),
            Map.entry("camera", Sound.tone(Wave.NOISE, 0, 0, 0.03, 0.08).then(Sound.tone(Wave.SQUARE, 1400, 1400, 0.02, 0.03))));

    /** The sound of that name, or null for one this game does not have. */
    Sound get(String name) {
        return sounds.get(name);
    }
}
