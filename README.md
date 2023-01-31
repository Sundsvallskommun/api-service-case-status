# CaseStatus

## Leverantör
Sundsvalls Kommun

## Beskrivning
Ett standardiserat status-API (status ur intressentens perspektiv), som levererar resultatet på ett för OeP-optimerat format, så väl som på andra format. Cachar ärenden ifrån OpenE för att snabbare kunna tillhandahålla status för ett ärende.

## Tekniska detaljer

### Konfiguration

Konfiguration sker i filen `src/main/resources/application.properties` genom att sätta nedanstående properties till önskade värden:

|Property|Beskrivning|
|---|---|
|**Incident integration**||
|`integration.incident.base-url`|API-URL till Incident-tjänsten
|`integration.incident.oauth2.token-uri`|URL för att hämta OAuth2-token för EmailSender-tjänsten
|`integration.incident.oauth2.client-id`|OAuth2-klient-id för Incident-tjänsten
|`integration.incident.oauth2.client-secret`|OAuth2-klient-nyckel Incident-tjänsten
|**Citizen integration**||
|`integration.citizen.base-url`|API-URL till Citizen-tjänsten
|`integration.citizen.oauth2.token-uri`|URL för att hämta OAuth2-token för Citizen-tjänsten
|`integration.citizen.oauth2.client-id`|OAuth2-klient-id för Citizen-tjänsten
|`integration.citizen.oauth2.client-secret`|OAuth2-klient-nyckel Citizen-tjänsten
|**CaseManagement integration**||
|`integration.case-management.base-url`|API-URL till CaseManagement-tjänsten
|`integration.case-management.oauth2.token-uri`|URL för att hämta OAuth2-token för CaseManagement-tjänsten
|`integration.case-management.oauth2.client-id`|OAuth2-klient-id för CaseManagement-tjänsten
|`integration.case-management.oauth2.client-secret`|OAuth2-klient-nyckel CaseManagement-tjänsten
|**OpenE integration**||
|`integration.open-e.base-url`|API-URL till OpenE-plattformen
|`integration.open-e.scheme=`|Om HTTP eller HTTPS ska användas för OpenE-plattformen
|`integration.open-e.port`|Port för för OpenE-plattformen
|`integration.open-e.basic-auth.username`|Användarnamn för OpenE-plattformen
|`integration.open-e.basic-auth.password`|Lösenord för OpenE-plattformen
|**Databasinställningar**||
|`integration.db.case-status.driver-class`|Den JDBC-driver som ska användas
|`integration.db.case-status.url`|JDBC-URL för anslutning till databas
|`integration.db.case-status.username`|Användarnamn för anslutning till databas
|`integration.db.case-status.password`|Lösenord för anslutning till databas
|**Casestatus Cache Scheduling**||
|`cache.scheduled.initialdelay`|Hur snabbt cachning ska köras efter uppstart
|`cache.scheduled.fixedrate`|Hur ofta cachning ska köras
|`cache.isprod`|boolean för att avgöra om applikationen körs i produktionsläge

### Paketera och starta tjänsten

Paketera tjänsten som en körbar JAR-fil genom:

```
mvn package
```

Starta med:

```
java -jar target/api-service-casestatus-<VERSION>.jar
```

### Bygga och starta tjänsten med Docker

Bygg en Docker-image av tjänsten:

```
mvn spring-boot:build-image
```

Starta en Docker-container:

```
docker run -i --rm -p 8080:8080 evil.sundsvall.se/ms-casestatus:latest
```


Copyright &copy; 2022 Sundsvalls Kommun
