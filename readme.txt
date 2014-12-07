standalone config ändern:

<address-settings>
    <address-setting match="#">
       <dead-letter-address>jms.queue.DLQ</dead-letter-address>
       <expiry-address>jms.queue.ExpiryQueue</expiry-address>
===>   <max-delivery-attempts>-1</max-delivery-attempts>
       <max-size-bytes>10485760</max-size-bytes>
       <page-size-bytes>2097152</page-size-bytes>
       <message-counter-history-day-limit>10</message-counter-history-day-limit>
    </address-setting>
</address-settings>

wenn "max-delivery-attempts" nicht auf -1 gesetzt wird würde jede nach einer
gewissen Anzahl von zustellungs versuchen die Nachricht vom Server gelöscht.
Jedes rollback wird als ein Versuch gewerted, also wenn ein Arbeiter in 10
(Default Wert) Versuchen seine Materialien nicht zusammen bekommt, werden die
welche er genommenund und wieder zurückgelegt hat (rollback) vom Server gelöscht

Löschen der Queue Daten:
Unter Windows ist im standalone Ordner ein "daten" Ordner, welcher gelöscht
werden kann und die Queues werden gelöscht.
