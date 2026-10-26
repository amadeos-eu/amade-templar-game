package eu.amadeos.games.templar.ui.art;

import eu.amadeos.games.templar.ui.art.Look.Beard;
import eu.amadeos.games.templar.ui.art.Look.Extra;
import eu.amadeos.games.templar.ui.art.Look.Garment;
import eu.amadeos.games.templar.ui.art.Look.Hair;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static eu.amadeos.games.templar.ui.art.Brush.c;

/** The people of the story and how they look; the ids are the names in the scripts. */
public final class Cast {
    private Cast() {
    }

    // ---- today ----

    // --8<-- [start:maria]
    /** Maria Vogt, book restorer from Leipzig: navy roll-neck, dark trousers, long auburn hair, glasses. */
    public static final Look MARIA = new Look("maria", 115, 1.02, true, Look.SKIN_LIGHT, c(0x7a3a20), Hair.LONG,
            Beard.NONE, c(0x2e3548), c(0x2e3548), c(0x2c2e36), c(0x4a2a1a), Garment.APRON, c(0xddcba6), 34,
            EnumSet.of(Extra.GLASSES, Extra.ROLL_NECK), c(0x2e5a6a));

    /** Maria outdoors: her rust-red coat and the teal scarf from the hook in the workshop. */
    public static final Look MARIA_OUT = MARIA.wearing(Garment.COAT, c(0x7a2e28), 38,
            EnumSet.of(Extra.GLASSES, Extra.ROLL_NECK, Extra.SCARF));
    // --8<-- [end:maria]

    /** Monsieur Lefort, the archivist: bald with a grey fringe, round glasses, tweed and a bow tie. */
    public static final Look LEFORT = new Look("lefort", 110, 1.06, false, Look.SKIN_FAIR, c(0xb8b4ac), Hair.FRINGE,
            Beard.MOUSTACHE, c(0xc8a040), c(0xeee8da), c(0x5a5650), c(0x2a1a12), Garment.JACKET, c(0x6a5038), 52,
            EnumSet.of(Extra.ROUND_GLASSES, Extra.BOWTIE), c(0x8a2028));

    /** "Treasure Kev": cap, hoodie, jeans, white trainers, and the phone on its stick. */
    public static final Look KEV = new Look("kev", 121, 1.04, false, Look.SKIN_LIGHT, c(0xc89050), Hair.SHORT,
            Beard.STUBBLE, c(0xe07a20), c(0xe07a20), c(0x3a5a8a), c(0xf2f0ea), Garment.NONE, c(0), 0,
            EnumSet.of(Extra.CAP, Extra.HOOD, Extra.SELFIE_STICK), c(0xb02a2a));

    /** Hélène de Vaucresson, the collector: dark chignon, a long camel coat, a green silk scarf, heels. */
    public static final Look HELENE = new Look("helene", 119, 0.92, true, Look.SKIN_FAIR, c(0x2a1a14), Hair.CHIGNON,
            Beard.NONE, c(0x1a1a1e), c(0x1a1a1e), c(0x1a1a1e), c(0x141010), Garment.COAT, c(0xc09a68), 26,
            EnumSet.of(Extra.SCARF, Extra.HANDBAG, Extra.EARRINGS), c(0x2a6a4a));

    /** Hélène's driver: big, dark suit, sunglasses even at night. */
    public static final Look DRIVER = new Look("driver", 127, 1.22, false, Look.SKIN_OLIVE, c(0x1a1410), Hair.CROPPED,
            Beard.NONE, c(0xf0f0f0), c(0x1c1e24), c(0x1c1e24), c(0x0e0e10), Garment.JACKET, c(0x1c1e24), 52,
            EnumSet.of(Extra.SUNGLASSES), c(0x1c1e24));

    /** The tour guide on the Pont Neuf, umbrella up so her group can follow. */
    public static final Look GUIDE = new Look("guide", 112, 1.0, true, Look.SKIN_OLIVE, c(0x3a2418), Hair.SHORT,
            Beard.NONE, c(0x2a6aa0), c(0x2a6aa0), c(0x3a3a40), c(0x2a2a2a), Garment.NONE, c(0), 0,
            EnumSet.of(Extra.UMBRELLA, Extra.SCARF), c(0xe8c040));

    public static final Look TOURIST_A = new Look("tourist", 117, 1.1, false, Look.SKIN_LIGHT, c(0xd8c8a8), Hair.CROPPED,
            Beard.NONE, c(0xd84a4a), c(0xd84a4a), c(0xc8b890), c(0xf0f0f0), Garment.NONE, c(0), 0,
            EnumSet.of(Extra.CAP, Extra.SUNGLASSES), c(0xf0f0f0));

    public static final Look TOURIST_B = new Look("tourist", 108, 0.98, true, Look.SKIN_FAIR, c(0xe0d0b0), Hair.SHORT,
            Beard.NONE, c(0x6a9a5a), c(0x6a9a5a), c(0x5a5a6a), c(0x8a6a4a), Garment.NONE, c(0), 0,
            EnumSet.of(Extra.HANDBAG, Extra.GLASSES), c(0x6a9a5a));

    /** The old fisherman of La Rochelle: grey beard, a sailor's cap, a yellow oilskin over a navy jumper. */
    public static final Look FISHERMAN = new Look("fisherman", 114, 1.12, false, Look.SKIN_WEATHERED, c(0xc8c4bc),
            Hair.SHORT, Beard.FULL, c(0x2a3a5a), c(0x2a3a5a), c(0x3a3a3a), c(0x2a2a2a), Garment.COAT, c(0xe0aa1c), 40,
            EnumSet.of(Extra.CAP), c(0x1a2a4a));

    /** Madame Morin of the café in the Cour du Temple: grey chignon, plum dress, a white apron, glasses. */
    public static final Look MORIN = new Look("morin", 108, 1.08, true, Look.SKIN_FAIR, c(0xb8b0a8), Hair.CHIGNON,
            Beard.NONE, c(0x5a2a3a), c(0x5a2a3a), c(0x2a2a2a), c(0x2a1a12), Garment.APRON, c(0xf0ece0), 30,
            EnumSet.of(Extra.GLASSES), c(0x5a2a3a));

    /** Dona Inês, the guide at the Convent of Christ: black hair in a chignon, a navy blazer, a red scarf. */
    public static final Look INES = new Look("ines", 109, 1.06, true, Look.SKIN_OLIVE, c(0x1e1612), Hair.CHIGNON,
            Beard.NONE, c(0xf0ece4), c(0x23304c), c(0x23304c), c(0x1a1414), Garment.JACKET, c(0x23304c), 44,
            EnumSet.of(Extra.SCARF, Extra.EARRINGS, Extra.GLASSES), c(0xa8201c));

    // ---- 1307 ----

    /** Aimery, a squire of sixteen: brown tunic with belt and pouch, green hose, a bowl cut. */
    public static final Look AIMERY = new Look("aimery", 106, 0.92, false, Look.SKIN_LIGHT, c(0x6a4426), Hair.BOWL,
            Beard.NONE, c(0x5a3c22), c(0x5a3c22), c(0x3a4a2e), c(0x3a2414), Garment.TUNIC, c(0x5a3c22), 50,
            EnumSet.of(Extra.BELT, Extra.POUCH), c(0x5a3c22));

    /** Brother Gérard, treasurer of the Paris Temple: white habit and mantle with the red cross, grey beard. */
    public static final Look GERARD = new Look("gerard", 117, 1.05, false, Look.SKIN_WEATHERED, c(0x9a948a), Hair.CROPPED,
            Beard.FULL, c(0xe8e2d4), c(0xe8e2d4), c(0xe8e2d4), c(0x3a2a1c), Garment.MANTLE, c(0xf2eee4), 5,
            EnumSet.of(Extra.CROSS, Extra.BELT), c(0xb0201a));

    /** The sergeant at the gate: dark habit with the red cross, a kettle hat, a spear, very tired. */
    public static final Look GUARD = new Look("guard", 122, 1.15, false, Look.SKIN_WEATHERED, c(0x4a3020), Hair.CROPPED,
            Beard.FULL, c(0x2a2622), c(0x8a8e96), c(0x3a3630), c(0x2a1c12), Garment.SURCOAT, c(0x2a2622), 30,
            EnumSet.of(Extra.CROSS, Extra.HELMET, Extra.SPEAR), c(0xb0201a));

    /** The king's men who come at dawn: blue surcoats with golden lilies over mail. */
    public static final Look SOLDIER = new Look("soldier", 123, 1.12, false, Look.SKIN_OLIVE, c(0x2a1a10), Hair.CROPPED,
            Beard.STUBBLE, c(0x8a8e96), c(0x8a8e96), c(0x8a8e96), c(0x2a1c12), Garment.SURCOAT, c(0x22388a), 30,
            EnumSet.of(Extra.LILIES, Extra.COIF, Extra.TORCH), c(0x22388a));

    /** Brother Raoul after the arrests: no helmet and no cross, a carter's brown hood over his habit. */
    public static final Look RAOUL = new Look("raoul", 122, 1.15, false, Look.SKIN_WEATHERED, c(0x4a3020), Hair.CROPPED,
            Beard.FULL, c(0x5a4a36), c(0x5a4a36), c(0x3a3630), c(0x2a1c12), Garment.MANTLE, c(0x6a5436), 30,
            EnumSet.of(Extra.HOOD, Extra.BELT), c(0x6a5436));

    /** Raoul on the quay at La Rochelle, with a shuttered lantern. */
    public static final Look RAOUL_LANTERN = RAOUL.wearing(Garment.MANTLE, c(0x6a5436), 30,
            EnumSet.of(Extra.HOOD, Extra.BELT, Extra.LANTERN));

    /** Brother Étienne of the Templars' house at La Rochelle: white mantle, brown beard. */
    public static final Look ETIENNE = new Look("etienne", 115, 1.0, false, Look.SKIN_LIGHT, c(0x5a3a20), Hair.CROPPED,
            Beard.FULL, c(0xe8e2d4), c(0xe8e2d4), c(0xe8e2d4), c(0x3a2a1c), Garment.MANTLE, c(0xf2eee4), 5,
            EnumSet.of(Extra.CROSS, Extra.BELT), c(0xb0201a));

    /** The Portuguese captain: a red tunic, a dark beard, a belt with a pouch. */
    public static final Look CAPTAIN = new Look("captain", 118, 1.1, false, Look.SKIN_OLIVE, c(0x1a1410), Hair.SHORT,
            Beard.FULL, c(0x6a2a22), c(0x6a2a22), c(0x3a3428), c(0x2a1a10), Garment.TUNIC, c(0x6a2a22), 50,
            EnumSet.of(Extra.BELT, Extra.POUCH), c(0x6a2a22));

    private static final Map<String, Look> BY_ID = Map.ofEntries(
            Map.entry("maria", MARIA), Map.entry("lefort", LEFORT), Map.entry("kev", KEV), Map.entry("helene", HELENE),
            Map.entry("driver", DRIVER), Map.entry("guide", GUIDE), Map.entry("tourist1", TOURIST_A),
            Map.entry("tourist2", TOURIST_B), Map.entry("aimery", AIMERY), Map.entry("gerard", GERARD),
            Map.entry("guard", GUARD), Map.entry("soldier", SOLDIER), Map.entry("soldier2", SOLDIER),
            Map.entry("gerard-cart", GERARD), Map.entry("soldier-cart", SOLDIER), Map.entry("raoul", RAOUL),
            Map.entry("fisherman", FISHERMAN), Map.entry("kev-harbour", KEV), Map.entry("kev-court", KEV),
            Map.entry("morin", MORIN), Map.entry("etienne", ETIENNE), Map.entry("captain", CAPTAIN),
            Map.entry("ines", INES), Map.entry("ines-archive", INES), Map.entry("ines-cloister", INES),
            Map.entry("kev-tomar", KEV), Map.entry("kev-cloister", KEV), Map.entry("helene-tomar", HELENE),
            Map.entry("helene-archive", HELENE), Map.entry("helene-cloister", HELENE));

    /** Rooms where Maria has her coat on. */
    private static final Set<String> OUTDOORS = Set.of("square", "bridge", "harbour", "court", "cloister");

    /** The look of a person in a room, or null for an animal or someone drawn otherwise. */
    public static Look of(String id, String room) {
        if (id.equals("maria") && OUTDOORS.contains(room)) return MARIA_OUT;
        if (id.equals("raoul") && room.equals("quay")) return RAOUL_LANTERN;
        return BY_ID.get(id);
    }
}
