package eu.amadeos.games.templar.ui.art;

import java.awt.Color;
import java.util.EnumSet;
import java.util.Set;

import static eu.amadeos.games.templar.ui.art.Brush.c;

/**
 * How a person looks: size, build, skin, hair and clothes. {@link Figure} draws any look in any pose and from
 * three sides, so a new character is a new look, not new drawing code.
 *
 * @param height  in logical pixels at depth scale 1, head to sole
 * @param build   1 for an average build; broader shoulders and hips above, narrower below
 * @param hem     how high above the soles the garment ends, in pixels at height 118
 */
public record Look(String name, double height, double build, boolean female, Color skin, Color hair, Hair hairStyle,
                   Beard beard, Color top, Color sleeve, Color legs, Color shoes, Garment garment, Color garmentColor,
                   double hem, Set<Extra> extras, Color accent) {

    public enum Hair { LONG, SHORT, BOWL, FRINGE, CHIGNON, CROPPED, BALD }

    public enum Beard { NONE, STUBBLE, FULL, MOUSTACHE }

    /** What is worn over the top and the legs. */
    public enum Garment {
        NONE,
        /** A workshop apron from the chest to the knees. */
        APRON,
        /** An open coat over the top, down to the hem. */
        COAT,
        /** A belted tunic down to the hem. */
        TUNIC,
        /** A habit to the ankles. */
        ROBE,
        /** A habit with a cloak over the shoulders. */
        MANTLE,
        /** A sleeveless surcoat over mail. */
        SURCOAT,
        /** A blazer, a waistcoat underneath. */
        JACKET
    }

    public enum Extra {
        GLASSES, ROUND_GLASSES, SUNGLASSES, CROSS, LILIES, CAP, HELMET, COIF, SPEAR, SELFIE_STICK, HANDBAG, BOWTIE,
        SCARF, UMBRELLA, BELT, POUCH, ROLL_NECK, HOOD, TORCH, EARRINGS
    }

    public Look {
        extras = extras.isEmpty() ? EnumSet.noneOf(Extra.class) : EnumSet.copyOf(extras);
    }

    public boolean has(Extra extra) {
        return extras.contains(extra);
    }

    /** A copy with other clothes over the same person, as Lena without her apron outdoors. */
    public Look wearing(Garment garment, Color color, double hem, Set<Extra> extras) {
        return new Look(name, height, build, female, skin, hair, hairStyle, beard, top, sleeve, legs, shoes, garment,
                color, hem, extras, accent);
    }

    static final Color SKIN_LIGHT = c(0xf1c7a3);
    static final Color SKIN_FAIR = c(0xf4d2b8);
    static final Color SKIN_OLIVE = c(0xd6a57c);
    static final Color SKIN_WEATHERED = c(0xd9a888);
}
