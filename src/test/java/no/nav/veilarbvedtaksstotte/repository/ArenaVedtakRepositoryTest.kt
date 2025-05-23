package no.nav.veilarbvedtaksstotte.repository

import no.nav.common.types.identer.Fnr
import no.nav.veilarbvedtaksstotte.domain.vedtak.ArenaVedtak
import no.nav.veilarbvedtaksstotte.utils.DatabaseTest
import no.nav.veilarbvedtaksstotte.utils.TimeUtils.now
import org.apache.commons.lang3.RandomStringUtils.randomNumeric
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ArenaVedtakRepositoryTest : DatabaseTest() {


    companion object {
        lateinit var arenaVedtakRepository: ArenaVedtakRepository

        @BeforeAll
        @JvmStatic
        fun setup() {
            arenaVedtakRepository = ArenaVedtakRepository(jdbcTemplate)
        }
    }

    @Test
    fun `lagre, oppdatere og hente arena-vedtak`() {
        val fnr = Fnr(randomNumeric(10))
        val forventetOpprinneligVedtak = ArenaVedtak(
            fnr = fnr,
            innsatsgruppe = ArenaVedtak.ArenaInnsatsgruppe.BATT,
            hovedmal = ArenaVedtak.ArenaHovedmal.SKAFFEA,
            fraDato = LocalDate.now(),
            regUser = "reg user",
            operationTimestamp = now(),
            hendelseId = 12345,
            vedtakId = 1
        )

        arenaVedtakRepository.upsertVedtak(forventetOpprinneligVedtak)

        val lagretVedtak = arenaVedtakRepository.hentVedtak(fnr)

        assertEquals(forventetOpprinneligVedtak, lagretVedtak)

        val forventetOppdatertVedtak = forventetOpprinneligVedtak
            .copy(
                innsatsgruppe = ArenaVedtak.ArenaInnsatsgruppe.VARIG,
                hovedmal = ArenaVedtak.ArenaHovedmal.OKEDELT,
                fraDato = forventetOpprinneligVedtak.fraDato.plusDays(1),
                regUser = "reg user 2",
                operationTimestamp = forventetOpprinneligVedtak.operationTimestamp.plusHours(2),
                hendelseId = forventetOpprinneligVedtak.hendelseId + 1,
                vedtakId = forventetOpprinneligVedtak.vedtakId + 2,
            )

        arenaVedtakRepository.upsertVedtak(forventetOppdatertVedtak)

        val lagretOppdatertVedtak = arenaVedtakRepository.hentVedtak(fnr)

        assertEquals(forventetOppdatertVedtak, lagretOppdatertVedtak)
    }

    @Test
    fun `upsert oppdaterer forventet arena-vedtak`() {
        val arenaVedtak1 = ArenaVedtak(
            fnr = Fnr(randomNumeric(10)),
            innsatsgruppe = ArenaVedtak.ArenaInnsatsgruppe.BATT,
            hovedmal = ArenaVedtak.ArenaHovedmal.SKAFFEA,
            fraDato = LocalDate.now(),
            regUser = "reg user",
            operationTimestamp = now(),
            hendelseId = 12345,
            vedtakId = 1
        )
        val arenaVedtak2 = arenaVedtak1.copy(fnr = Fnr(randomNumeric(10)))

        arenaVedtakRepository.upsertVedtak(arenaVedtak1)
        arenaVedtakRepository.upsertVedtak(arenaVedtak2)

        assertEquals(arenaVedtak1, arenaVedtakRepository.hentVedtak(arenaVedtak1.fnr))
        assertEquals(arenaVedtak2, arenaVedtakRepository.hentVedtak(arenaVedtak2.fnr))

        val oppdatertArenaVedtak2 = arenaVedtak2.copy(fraDato = arenaVedtak2.fraDato.plusDays(2))
        arenaVedtakRepository.upsertVedtak(oppdatertArenaVedtak2)

        assertEquals(arenaVedtak1, arenaVedtakRepository.hentVedtak(arenaVedtak1.fnr))
        assertEquals(oppdatertArenaVedtak2, arenaVedtakRepository.hentVedtak(arenaVedtak2.fnr))
    }

    @Test
    fun `hent liste av arena-vedtak basert på liste av fnr`() {
        val arenaVedtak1 = ArenaVedtak(
            fnr = Fnr(randomNumeric(10)),
            innsatsgruppe = ArenaVedtak.ArenaInnsatsgruppe.BATT,
            hovedmal = ArenaVedtak.ArenaHovedmal.SKAFFEA,
            fraDato = LocalDate.now(),
            regUser = "reg user",
            operationTimestamp = now(),
            hendelseId = 12345,
            vedtakId = 1
        )
        val arenaVedtak2 = arenaVedtak1.copy(fnr = Fnr(randomNumeric(10)))
        val arenaVedtak3 = arenaVedtak1.copy(fnr = Fnr(randomNumeric(10)))
        arenaVedtakRepository.upsertVedtak(arenaVedtak1)
        arenaVedtakRepository.upsertVedtak(arenaVedtak2)
        arenaVedtakRepository.upsertVedtak(arenaVedtak3)

        val hentVedtakListe =
            arenaVedtakRepository.hentVedtakListe(listOf(arenaVedtak1.fnr, arenaVedtak3.fnr))


        assertThat(hentVedtakListe, containsInAnyOrder(arenaVedtak1, arenaVedtak3))
    }
}
