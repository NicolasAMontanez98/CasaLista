package com.example.casalista.data

object SampleHouses {
    val all = listOf(
        House("aurora", "Casa Aurora", 68_000_000, 45, 2, "Steel framing", "Disponible"),
        House("brisa", "Casa Brisa", 89_000_000, 60, 2, "Steel framing", "Disponible"),
        House("cumbre", "Casa Cumbre", 120_000_000, 80, 3, "Panel SIP", "Disponible"),
        House("dalia", "Casa Dalia", 142_000_000, 95, 3, "Panel SIP", "Sobre pedido"),
        House("eucalipto", "Casa Eucalipto", 178_000_000, 110, 4, "Madera", "Disponible"),
        House("fenix", "Casa Fénix", 52_000_000, 36, 1, "Contenedor", "Disponible"),
        House("guadua", "Casa Guadua", 95_000_000, 70, 3, "Guadua", "Disponible"),
        House("horizonte", "Casa Horizonte", 210_000_000, 130, 4, "Steel framing", "Sobre pedido"),
        House("isla", "Casa Isla", 74_000_000, 55, 2, "Contenedor", "Agotada"),
        House("jacaranda", "Casa Jacaranda", 250_000_000, 150, 5, "Madera", "Disponible"),
        House("kiwi", "Casa Kiwi", 98_000_000, 65, 2, "Guadua", Status.REVIEW),
        House("laurel", "Casa Laurel", 135_000_000, 85, 3, "Madera", Status.REVIEW),
    )
}
