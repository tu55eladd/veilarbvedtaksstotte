package no.nav.veilarbvedtaksstotte.schedule

import io.getunleash.DefaultUnleash
import no.nav.common.job.JobRunner
import no.nav.common.job.leader_election.LeaderElectionClient
import no.nav.veilarbvedtaksstotte.repository.VedtaksstotteRepository
import no.nav.veilarbvedtaksstotte.service.DistribusjonService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class DistribuerJournalforteVedtakSchedule(
    val leaderElection: LeaderElectionClient,
    val distribusjonService: DistribusjonService,
    val vedtaksstotteRepository: VedtaksstotteRepository,
) {

    val log = LoggerFactory.getLogger(DistribuerJournalforteVedtakSchedule::class.java)

    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.MINUTES)
    fun distribuerJournalforteVedtak() {
        if (leaderElection.isLeader) {
            JobRunner.run("distribuer_journalforte_vedtak") {

                val vedtakForDistribusjon: MutableList<Long> = vedtaksstotteRepository.hentVedtakForDistribusjon(100)

                if (vedtakForDistribusjon.isEmpty()) {
                    log.info("Ingen nye vedtak å distribuere")
                } else {
                    log.info(
                        "Distribuerer ${vedtakForDistribusjon.size} vedtak med id: ${
                            vedtakForDistribusjon.joinToString(", ", "{", "}")
                        }"
                    )

                    vedtakForDistribusjon.forEach {
                        try {
                            distribusjonService.distribuerVedtak(it)
                        } catch (e: RuntimeException) {
                            log.error("Distribusjon av vedtak med id $it feilet", e)
                        }
                    }
                }
            }
        }
    }
}
