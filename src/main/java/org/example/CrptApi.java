package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class CrptApi {
    private  long timeMillisInterval;
    private  int requestLimit;
    private int requestCount = 0;
    private long lastRequestTime = 0;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.timeMillisInterval = timeUnit.toMillis(1);
        this.requestLimit = requestLimit;
    }

    public synchronized void createDocument(MyDoc document, String sign) throws InterruptedException, URISyntaxException, IOException {
        long currentTimeMillis = System.currentTimeMillis();
        if((currentTimeMillis - lastRequestTime) > timeMillisInterval) {
            requestCount = 0;
        }

        if(requestCount >= requestLimit) {
            List<Thread> timedWaitingThreads = Thread.getAllStackTraces().keySet().stream()
                    .filter((t) -> t.getState() == Thread.State.TIMED_WAITING).toList();
            int amountOfOurWaitingThreads = timedWaitingThreads.size() - 1;

            int amountOfIntervalsToWait = amountOfOurWaitingThreads / requestLimit;

            wait( timeMillisInterval - (currentTimeMillis - lastRequestTime) + timeMillisInterval * amountOfIntervalsToWait);

        }


        //API CALL
        var client = HttpClient.newHttpClient();
        URI uri = new URI("https://ismp.crpt.ru/api/v3/lk/documents/create");

        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule()); // support Java 8 date time apis

        String jsonInputString = om.writeValueAsString(document);

        var request = HttpRequest.newBuilder(uri).
                POST(HttpRequest.BodyPublishers.ofString(jsonInputString))
                .header("Content-type", "application/json").
                header("Signature", sign).
                build();
        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());

        lastRequestTime = System.currentTimeMillis();
        System.out.println(String.format("API CALL AT %d with response: %s",lastRequestTime, response ));
        requestCount++;

    }


    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {

        CrptApi crptApi = new CrptApi(TimeUnit.SECONDS, 3);
        MyDoc myDoc = new MyDoc(new Description("participantInn"), "doc_id", "doc_status", "LP_INTRODUCE_GOODS", true, "owner_inn", "participant_inn", "producer_inn", LocalDate.parse("2020-01-23"), "production_type", List.of(new Product("certificate_document", LocalDate.parse("2020-01-23"), "certificate_document_number", "owner_inn", "producer_inn", LocalDate.parse("2020-01-23"), "tnved_code", "uit_code", "uitu_code")),LocalDate.parse("2020-01-23"), "reg_number");
        String sign = "sign";

        crptApi.createDocument(myDoc, sign);

    }

    static class MyDoc {
        private Description description;
        private String doc_id;
        private String doc_status;
        private String doc_type ;
        private boolean importRequest ;
        private String owner_inn;
        private String participant_inn;
        private String producer_inn;

        private LocalDate production_date ;
        private String production_type;
        private List<Product> products;
        private LocalDate reg_date ;
        private String reg_number;

        public MyDoc(Description description, String doc_id, String doc_status, String doc_type, boolean importRequest, String owner_inn, String participant_inn, String producer_inn, LocalDate production_date, String production_type, List<Product> products, LocalDate reg_date, String reg_number) {
            this.description = description;
            this.doc_id = doc_id;
            this.doc_status = doc_status;
            this.doc_type = doc_type;
            this.importRequest = importRequest;
            this.owner_inn = owner_inn;
            this.participant_inn = participant_inn;
            this.producer_inn = producer_inn;
            this.production_date = production_date;
            this.production_type = production_type;
            this.products = products;
            this.reg_date = reg_date;
            this.reg_number = reg_number;
        }

        @Override
        public String toString() {
            return "MyDoc{" +
                    "description=" + description +
                    ", doc_id='" + doc_id + '\'' +
                    ", doc_status='" + doc_status + '\'' +
                    ", doc_type='" + doc_type + '\'' +
                    ", importRequest=" + importRequest +
                    ", owner_inn='" + owner_inn + '\'' +
                    ", participant_inn='" + participant_inn + '\'' +
                    ", producer_inn='" + producer_inn + '\'' +
                    ", production_date=" + production_date +
                    ", production_type='" + production_type + '\'' +
                    ", products=" + products +
                    ", reg_date=" + reg_date +
                    ", reg_number='" + reg_number + '\'' +
                    '}';
        }

        public Description getDescription() {
            return description;
        }

        public String getDoc_id() {
            return doc_id;
        }

        public String getDoc_status() {
            return doc_status;
        }

        public String getDoc_type() {
            return doc_type;
        }

        public boolean isImportRequest() {
            return importRequest;
        }

        public String getOwner_inn() {
            return owner_inn;
        }

        public String getParticipant_inn() {
            return participant_inn;
        }

        public String getProducer_inn() {
            return producer_inn;
        }

        public LocalDate getProduction_date() {
            return production_date;
        }

        public String getProduction_type() {
            return production_type;
        }

        public List<Product> getProducts() {
            return products;
        }

        public LocalDate getReg_date() {
            return reg_date;
        }

        public String getReg_number() {
            return reg_number;
        }
    }
    static class Description {

        private String participantInn;

        public Description(String participantInn) {
            this.participantInn = participantInn;
        }

        public String getParticipantInn() {
            return participantInn;
        }

        public void setParticipantInn(String participantInn) {
            this.participantInn = participantInn;
        }

        @Override
        public String toString() {
            return "Description{" +
                    "participantInn='" + participantInn + '\'' +
                    '}';
        }
    }

    static class Product {
        private String certificate_document;
        private LocalDate certificate_document_date ;
        private String certificate_document_number;
        private String owner_inn;
        private String producer_inn;
        private LocalDate production_date ;
        private String tnved_code;
        private String uit_code;
        private String uitu_code;

        public Product(String certificate_document, LocalDate certificate_document_date, String certificate_document_number, String owner_inn, String producer_inn, LocalDate production_date, String tnved_code, String uit_code, String uitu_code) {
            this.certificate_document = certificate_document;
            this.certificate_document_date = certificate_document_date;
            this.certificate_document_number = certificate_document_number;
            this.owner_inn = owner_inn;
            this.producer_inn = producer_inn;
            this.production_date = production_date;
            this.tnved_code = tnved_code;
            this.uit_code = uit_code;
            this.uitu_code = uitu_code;
        }

        public String getCertificate_document() {
            return certificate_document;
        }

        public LocalDate getCertificate_document_date() {
            return certificate_document_date;
        }

        public String getCertificate_document_number() {
            return certificate_document_number;
        }

        public String getOwner_inn() {
            return owner_inn;
        }

        public String getProducer_inn() {
            return producer_inn;
        }

        public LocalDate getProduction_date() {
            return production_date;
        }

        public String getTnved_code() {
            return tnved_code;
        }

        public String getUit_code() {
            return uit_code;
        }

        public String getUitu_code() {
            return uitu_code;
        }

        @Override
        public String toString() {
            return "Product{" +
                    "certificate_document='" + certificate_document + '\'' +
                    ", certificate_document_date=" + certificate_document_date +
                    ", certificate_document_number='" + certificate_document_number + '\'' +
                    ", owner_inn='" + owner_inn + '\'' +
                    ", producer_inn='" + producer_inn + '\'' +
                    ", production_date=" + production_date +
                    ", tnved_code='" + tnved_code + '\'' +
                    ", uit_code='" + uit_code + '\'' +
                    ", uitu_code='" + uitu_code + '\'' +
                    '}';
        }
    }

}
