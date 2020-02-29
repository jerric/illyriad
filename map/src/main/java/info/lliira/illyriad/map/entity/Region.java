package info.lliira.illyriad.map.entity;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * tag: c
 */
public enum Region {
    UNKNOWN(0),
    TheWastes(1),
    KalTirikan(2),
    Wolgast(3),
    Ursor(4),
    Qarosslan(5),
    Windlost(6),
    Tamarin(7),
    Fremorn(8),
    Norweld(9),
    Laoshin(10),
    Ragallon(11),
    Taomist(12),
    Meilla(13),
    Lucerna(14),
    MiddleKingdom(15),
    MalMotsha(16),
    Keppen(17),
    TorCarrock(18),
    TheWesternRealms(19),
    Keshalia(20),
    Perrigor(21),
    KulTar(22),
    Kumala(23),
    LanLarosh(24),
    Arran(25),
    Turalia(26),
    Zanpur(27),
    Elijal(28),
    Azura(29),
    Djebeli(30),
    Ocean(31),
    Tallimar(32),
    Larn(33),
    Kem(34),
    FarraIsle(35),
    Trome(36),
    RillArchipelago(37),
    StormstoneIsland(38),
    Unknown_1(39),
    Calumnex(40),
    Puchuallpa(41),
    Unknown_2(42),
    Pamanyallpa(43),
    Huronire(44),
    Clarien(45),
    Pawanallpa(46),
    Unknown_3(47),
    ThePoisonedIsle(48),
    Glanhad(49),
    Northmarch(50),
    HighHills(51),
    Westmarch(52),
    Unknown_4(53),
    Oarnamly(54),
    Unknown_5(55),
    Gremont(56),
    Coanhara(57),
    LapoALua(58),
    Newlands(59),
    Unknown_6(60),
    Aindara(61),
    ThePirateIsles(62),
    Silbeaur(63),
    Fellandire(64),
    Unknown_7(65),
    Vindorel(66),
    Almenly(67),
    Kormandly(68),
    TheOrkenCoast(69),
    Kingslands(70),
    Farshards(71),
    Shardlands(72),
    Strendur(73),
    Chulbran(74),
    Jurgor(75),
    TheLongWhite(76),
    Unknown_8(77);

    private static final Map<Integer, Region> REGIONS =
        Arrays.stream(values()).collect(Collectors.toMap(region -> region.code, region -> region));

    public static Region parse(int code) {
        return REGIONS.getOrDefault(code, UNKNOWN);
    }

    public final int code;

    Region(int code) {
        this.code = code;
    }
}
