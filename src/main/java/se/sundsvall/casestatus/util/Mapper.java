package se.sundsvall.casestatus.util;

import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import se.sundsvall.casestatus.integration.db.domain.CacheCompanyCaseStatus;
import se.sundsvall.casestatus.integration.db.domain.CachePrivateCaseStatus;
import se.sundsvall.casestatus.integration.db.domain.CacheUnknownCaseStatus;
import us.codecraft.xsoup.Xsoup;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class Mapper {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private String formatDateTime(String dateString) {
        return dateString.isEmpty() ? null : LocalDateTime.parse(dateString).format(DATE_TIME_FORMAT);
    }

    public CacheCompanyCaseStatus toCacheCompanyCaseStatus(Document statusObject, Document errandObject, String organisationNumber) {
        return CacheCompanyCaseStatus.builder()
                .withStatus(Xsoup.select(statusObject, "//status/name/text()").get())
                .withContentType(Xsoup.select(statusObject, "//status/contentType/text()").get())
                .withFlowInstanceID(Xsoup.select(errandObject, "//header/FlowInstanceID/text()").get())
                .withFamilyID(Xsoup.select(errandObject, "//header/flow/familyId/text()").get())
                .withErrandType(Xsoup.select(errandObject, "//header/flow/name/text()").get().trim())
                .withFirstSubmitted(formatDateTime(Xsoup.select(errandObject, "//header/FirstSubmitted/text()").get()))
                .withLastStatusChange(formatDateTime(Xsoup.select(errandObject, "//header/LastSubmitted/text()").get()))
                .withOrganisationNumber(organisationNumber)
                .build();

    }

    public CachePrivateCaseStatus toCachePrivateCaseStatus(Document statusObject, Document errandObject, String personId) {
        return CachePrivateCaseStatus.builder()
                .withStatus(Xsoup.select(statusObject, "//status/name/text()").get())
                .withContentType(Xsoup.select(statusObject, "//status/contentType/text()").get())
                .withFlowInstanceID(Xsoup.select(errandObject, "//header/FlowInstanceID/text()").get())
                .withFamilyID(Xsoup.select(errandObject, "//header/flow/familyId/text()").get())
                .withErrandType(Xsoup.select(errandObject, "//header/flow/name/text()").get().trim())
                .withFirstSubmitted(formatDateTime(Xsoup.select(errandObject, "//header/FirstSubmitted/text()").get()))
                .withLastStatusChange(formatDateTime(Xsoup.select(errandObject, "//header/LastSubmitted/text()").get()))
                .withPersonId(personId.replace("\"", ""))
                .build();
    }

    public CacheUnknownCaseStatus toCacheUnknowCaseStatus(Document statusObject, Document errandObject) {
        return CacheUnknownCaseStatus.builder()
                .withStatus(Xsoup.select(statusObject, "//status/name/text()").get())
                .withContentType(Xsoup.select(statusObject, "//status/contentType/text()").get())
                .withFlowInstanceID(Xsoup.select(errandObject, "//header/FlowInstanceID/text()").get())
                .withFamilyID(Xsoup.select(errandObject, "//header/flow/familyId/text()").get())
                .withErrandType(Xsoup.select(errandObject, "//header/flow/name/text()").get().trim())
                .withFirstSubmitted(formatDateTime(Xsoup.select(errandObject, "//header/FirstSubmitted/text()").get()))
                .withLastStatusChange(formatDateTime(Xsoup.select(errandObject, "//header/LastSubmitted/text()").get()))
                .build();
    }


}
