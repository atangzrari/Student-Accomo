package com.bac.homelink.data.database

import com.bac.homelink.data.entities.Listing
import com.bac.homelink.data.entities.User
import com.bac.homelink.utils.HashUtils

object SeedData {
    private const val DEFAULT_PASSWORD = "1234"

    private val landlords = listOf(
        Triple("Amina Property Group", "71100001", "landlord01@homelink.bw"),
        Triple("Kgosi Rentals", "71100002", "landlord02@homelink.bw"),
        Triple("Moyo Student Homes", "71100003", "landlord03@homelink.bw"),
        Triple("Seretse Lets", "71100004", "landlord04@homelink.bw"),
        Triple("Tshiamo Accommodation", "71100005", "landlord05@homelink.bw"),
        Triple("Pula Property Partners", "71100006", "landlord06@homelink.bw"),
        Triple("Naledi Rentals", "71100007", "landlord07@homelink.bw"),
        Triple("Gaborone Campus Homes", "71100008", "landlord08@homelink.bw"),
        Triple("Bonnington Estates", "71100009", "landlord09@homelink.bw"),
        Triple("Phakalane Lets", "71100010", "landlord10@homelink.bw")
    )

    fun generateStudents(): List<User> {
        val names = listOf(
            "Keabetswe Moyo", "Tshepiso Kgosi", "Bontle Seretse", "Kagiso Mothibi",
            "Lerato Molefe", "Onthatile Moeng", "Refilwe Seitshiro", "Mpho Gaboipelwe",
            "Olebogeng Selelo", "Naledi Modise", "Thabo Dube", "Kelebogile Ramotswe",
            "Boitumelo Molebatsi", "Tebogo Morake", "Kgomotso Radipotsane", "Atang Ndlovu",
            "Goitseone Tau", "Kabo Sechele", "Neo Moremi", "Masego Makgato",
            "Ofentse Motsumi", "Dineo Kgatlhane", "Kefilwe Pilane", "Katlego Moagi",
            "Tlotlo Montsho", "Karabo Ruele", "Lorato Thema", "Bakang Gaseitsiwe",
            "Gofaone Ramoroka", "Mothusi Setlhare", "Bonolo Matlapeng", "Lesego Phiri",
            "Tshepho Gabathuse", "Mmapula Kgosidintsi", "Phenyo Sebina", "Refentse Gaone",
            "Tiro Badubi", "Omphile Mosimane", "Kedibonye Motlaleng", "Rorisang Keitumetse",
            "Tumisang Tsogang", "Keneilwe Rapelang", "Mmoloki Thipe", "Ditiro Lecha",
            "Tsholofelo Baaitse", "Gaone Mmereki", "Oarabile Matlho", "Kealeboga Ditlhogo",
            "Wame Marumo", "Thato Kelebogile", "Amantle Motshabi", "Koketso Molefe",
            "Lame Mogapi", "Tapiwa Motshegwa", "Kagiso Serwalo", "Oratile Mooketsi",
            "Botshelo Nfila", "Mpho Kganela"
        )
        return names.mapIndexed { i, name ->
            val first = name.split(" ")[0].lowercase()
            User(
                studentId = "BAC${2024000 + i + 1}",
                fullName = name,
                email = "${first}${i + 1}@student.bac.bw",
                phone = "7${1000000 + ((i + 1) * 371293) % 9000000}",
                passwordHash = HashUtils.sha256(DEFAULT_PASSWORD),
                role = "STUDENT",
                institution = "Botswana Accountancy College"
            )
        }
    }

    fun generateLandlords(): List<User> = landlords.mapIndexed { i, landlord ->
        User(
            studentId = "LANDLORD${(i + 1).toString().padStart(2, '0')}",
            fullName = landlord.first,
            email = landlord.third,
            phone = landlord.second,
            passwordHash = HashUtils.sha256(DEFAULT_PASSWORD),
            role = "PROVIDER",
            institution = "HomeLink Provider"
        )
    }

    fun generateUsers(): List<User> = generateStudents() + generateLandlords()

    fun generateListings(): List<Listing> {
        data class AccommodationType(
            val type: String,
            val rooms: String,
            val sharing: String,
            val beds: Int,
            val baths: Int,
            val minPrice: Int,
            val maxPrice: Int
        )

        val types = listOf(
            AccommodationType("Single Room", "1 Room", "Private (No Sharing)", 1, 1, 800, 1800),
            AccommodationType("Bachelor Flat", "1 Room", "Private (No Sharing)", 1, 1, 1500, 3500),
            AccommodationType("Room in Multi-Res", "1 Room", "Sharing (2 people)", 1, 1, 600, 1400),
            AccommodationType("Studio Apartment", "1 Room", "Private (No Sharing)", 1, 1, 2000, 4000),
            AccommodationType("2-Bedroom Flat", "2 Rooms", "Private (No Sharing)", 2, 1, 3000, 6500),
            AccommodationType("Garden Cottage", "1 Room", "Private (No Sharing)", 1, 1, 1800, 3200)
        )

        val areas = listOf(
            "Block 3", "Block 5", "Block 6", "Block 7", "Block 8",
            "Phase 2", "Phase 4", "Mogoditshane", "Tlokweng", "Gaborone West",
            "Broadhurst", "Bonnington", "Extension 2", "Extension 14", "Phakalane"
        )

        val amenities = listOf(
            "WiFi,Water,Electricity,Parking",
            "WiFi,Water,Electricity,Security Guard",
            "Water,Electricity,Laundry",
            "WiFi,Water,Electricity,Furnished"
        )

        val images = listOf(
            "https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800",
            "https://images.unsplash.com/photo-1555854877-bab0e564b8d5?w=800",
            "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=800",
            "https://images.unsplash.com/photo-1554995207-c18c203602cb?w=800"
        )

        val dates = listOf("2026-05-15", "2026-06-01", "2026-06-15", "2026-07-01")
        val prefixes = listOf("Cozy", "Modern", "Affordable", "Spacious", "Secure")

        return (0 until 50).map { i ->
            val type = types[i % types.size]
            val area = areas[i % areas.size]
            val landlord = landlords[i % landlords.size]
            val price = type.minPrice + ((type.maxPrice - type.minPrice) * (i % 5) / 4)
            val streetNum = (i + 1) * 7

            Listing(
                title = "${prefixes[i % prefixes.size]} ${type.type} in $area",
                description = "Well-maintained ${type.type.lowercase()} in $area, Gaborone. Close to public transport and shops.",
                pricePerMonth = price,
                depositAmount = price,
                securityAmount = if (i % 3 == 0) price / 2 else 0,
                location = area,
                fullAddress = "$streetNum ${area} Street, $area, Gaborone, Botswana",
                accommodationType = type.type,
                roomCount = type.rooms,
                sharingArrangement = type.sharing,
                amenities = amenities[i % amenities.size],
                availabilityDate = dates[i % dates.size],
                imageUrl = images[i % images.size],
                imageUrl2 = images[(i + 1) % images.size],
                landlordName = landlord.first,
                landlordPhone = landlord.second,
                landlordEmail = landlord.third,
                bedroomsCount = type.beds,
                bathroomsCount = type.baths,
                additionalNotes = if (i % 4 == 0) "No pets. Quiet hours after 10pm." else ""
            )
        }
    }
}
