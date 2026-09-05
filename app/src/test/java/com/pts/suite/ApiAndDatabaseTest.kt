package com.pts.suite

import com.google.gson.Gson
import com.pts.suite.data.api.*
import com.pts.suite.data.db.*
import org.junit.Assert.*
import org.junit.Test

class ApiAndDatabaseTest {

    private val gson = Gson()

    @Test
    fun testLoginResponseJsonParsing() {
        val json = """
            {
                "success": true,
                "token": "test_jwt_token_12345",
                "user": {
                    "id": "usr_01",
                    "username": "prajwal",
                    "role": "admin",
                    "avatar_url": "https://hub.ptsmovies.online/avatar.jpg"
                }
            }
        """.trimIndent()

        val res = gson.fromJson(json, LoginResponse::class.java)
        assertTrue(res.success)
        assertEquals("test_jwt_token_12345", res.token)
        assertNotNull(res.user)
        assertEquals("prajwal", res.user?.username)
        assertEquals("admin", res.user?.role)
        assertEquals("https://hub.ptsmovies.online/avatar.jpg", res.user?.avatarUrl)
    }

    @Test
    fun testMediaLibraryResponseParsing() {
        val json = """
            {
                "success": true,
                "movies": [
                    {
                        "id": "mov_45",
                        "title": "45",
                        "year": "2025",
                        "rating": "7.8",
                        "filePath": "/DATA/Movies/45.mp4",
                        "fileName": "45.mp4",
                        "genres": ["Action", "Drama"]
                    }
                ],
                "series": [
                    {
                        "id": "ser_lucifer",
                        "title": "Lucifer",
                        "year": "2016",
                        "totalEpisodes": 93,
                        "seasons": {
                            "1": [
                                {
                                    "id": "ep_1",
                                    "season": 1,
                                    "episode": 1,
                                    "epCode": "S01E01",
                                    "fileName": "Lucifer.S01E01.mp4",
                                    "filePath": "/DATA/Series/Lucifer/S01E01.mp4",
                                    "size": "450 MB"
                                }
                            ]
                        }
                    }
                ]
            }
        """.trimIndent()

        val res = gson.fromJson(json, MediaLibraryResponse::class.java)
        assertTrue(res.success)
        assertEquals(1, res.movies.size)
        assertEquals("45", res.movies[0].title)
        assertEquals(2, res.movies[0].genres.size)
        assertEquals(1, res.series.size)
        assertEquals(1, res.series[0].seasons["1"]?.size)
        assertEquals("S01E01", res.series[0].seasons["1"]?.get(0)?.epCode)
    }

    @Test
    fun testVaultDocumentJsonParsing() {
        val json = """
            {
                "id": 1,
                "title": "HDFC Credit Card",
                "filename": "card_scan.jpg",
                "category_id": 2,
                "category_name": "Banking",
                "doc_type": "bank_card",
                "holder_name": "Prajwal",
                "doc_number": "1234 5678 9012 3456",
                "issuer": "HDFC",
                "expiry_date": "12/28",
                "extra_info": "CVV: 999"
            }
        """.trimIndent()

        val doc = gson.fromJson(json, VaultDocument::class.java)
        assertEquals(1, doc.id)
        assertEquals("HDFC Credit Card", doc.title)
        assertEquals(2, doc.categoryId)
        assertEquals("bank_card", doc.docType)
        assertEquals("1234 5678 9012 3456", doc.docNumber)
    }

    @Test
    fun testRoomEntitiesInstantiation() {
        val movieEntity = CachedMovieEntity(
            id = "m1",
            title = "Test Movie",
            year = "2026",
            rating = "8.5",
            poster = "https://poster.jpg",
            backdrop = "https://backdrop.jpg",
            description = "A great test movie.",
            duration = "120m",
            genresJson = "[\"Action\"]",
            cast = "Actor 1",
            filePath = "/Data/Movies/test.mp4",
            fileName = "test.mp4",
            size = "1.5 GB",
            mtime = 1787000000L
        )
        assertEquals("m1", movieEntity.id)
        assertEquals("Test Movie", movieEntity.title)

        val seriesEntity = CachedSeriesEntity(
            id = "s1",
            title = "Test Series",
            year = "2026",
            rating = "9.0",
            poster = "https://poster.jpg",
            backdrop = null,
            description = "Series desc",
            genresJson = "[\"Drama\"]",
            cast = null,
            seasonsJson = "{}",
            totalEpisodes = 10,
            mtime = 1787000000L
        )
        assertEquals("s1", seriesEntity.id)
        assertEquals(10, seriesEntity.totalEpisodes)

        val vaultDoc = CachedVaultDocEntity(
            id = 10,
            title = "Passport",
            filename = "pass.pdf",
            categoryId = 1,
            categoryName = "IDs",
            docType = "passport",
            holderName = "Prajwal",
            docNumber = "Z1234567",
            issuer = "India",
            expiryDate = "2030",
            extraInfo = ""
        )
        assertEquals(10, vaultDoc.id)
        assertEquals("passport", vaultDoc.docType)
    }
}
