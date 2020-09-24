package pps.covid_sim.model.places

object Locality {

  case class City(idCity: Int, name: String, numResidents: Int, province: Province) {
    val isProvince: Boolean = idCity == province.idProvince
  }

  case class Province(idProvince: Int, name: String, abbreviation: String, region: Region)

  case class Region(id: Int, name: String, numResidents: Int)

  object Province extends  Enumeration {
    type Province = Value

    val TORINO: Locality.Province = Province(1272,"Torino", "AB", Region.EMILIA_ROMAGNA)
    val BOLOGNA: Locality.Province = Province(2312,"Bologna", "BO", Region.EMILIA_ROMAGNA)

    val MILANO: Locality.Province = Province(5535,"Milano", "MI", Region.FRIULI_VENEZIA_GIULIA)
    val ROMA: Locality.Province = Province(7657,"Roma", "RO", Region.FRIULI_VENEZIA_GIULIA)
  }

  object Region extends Enumeration {

    type Region = Value

    val ABRUZZO: Locality.Region = Region(13, "Abruzzo", 1312507)
    val BASILICATA: Locality.Region = Region(17, "Basilicata", 576194)
    val CALABRIA: Locality.Region = Region(18, "Calabria", 1947938)
    val CAMPANIA: Locality.Region = Region(15, "Campania", 5769750)
    val EMILIA_ROMAGNA: Locality.Region = Region(8, "Emilia-Romagna", 4367138)
    val FRIULI_VENEZIA_GIULIA: Locality.Region = Region(6, "Friuli-Venezia Giulia", 1221901)
    val LAZIO: Locality.Region = Region(12, "Lazio", 5557276)
    val LIGURIA: Locality.Region = Region(7, "Liguria", 1565127)
    val LOMBARDIA: Locality.Region = Region(3, "Lombardia", 9783720)
    val MARCHE: Locality.Region = Region(11, "Marche", 1525945)
    val MOLISE: Locality.Region = Region(14, "Molise", 313341)
    val PIEMONTE: Locality.Region = Region(1, "Piemonte", 4379512)
    val PUGLIA: Locality.Region = Region(16, "Puglia", 4835695)
    val SARDEGNA: Locality.Region = Region(20, "Sardegna", 1640379)
    val SICILIA: Locality.Region = Region(19, "Sicilia", 4999932)
    val TOSCANA: Locality.Region = Region(9, "Toscana", 3676351)
    val TRENTINO_ALTO_ADIGE: Locality.Region = Region(4, "Trentino-Alto Adige", 1037264)
    val UMBRIA: Locality.Region = Region(10, "Umbria", 886239)
    val VALLE_DAOSTA: Locality.Region = Region(2, "Valle d'Aosta", 127844)
    val VENETO: Locality.Region = Region(5, "Veneto", 4881593)

  }

}
