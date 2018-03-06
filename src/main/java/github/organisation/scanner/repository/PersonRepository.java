package github.organisation.scanner.repository;

import github.organisation.scanner.ProcessGitHubOrganisation;
import github.organisation.scanner.model.Person;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;
import java.util.List;

public class PersonRepository {

    private RestTemplate restTemplate = new RestTemplate();

    public List<Person> getAllPersons(String organisation) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "token " + ProcessGitHubOrganisation.TOKEN);
        headers.add(HttpHeaders.ACCEPT, "application/vnd.github.v3+json");
        HttpEntity request = new HttpEntity(headers);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(ProcessGitHubOrganisation.GITHUB)
                .queryParam("type", "all")
                .queryParam("filter", "all")
                .queryParam("per_page", ProcessGitHubOrganisation.PAGES)
                .path("/orgs/{organisation}/members");
        URI uri = builder.build().expand(organisation).encode().toUri();
        ParameterizedTypeReference<List<Person>> returnType = new ParameterizedTypeReference<List<Person>>(){};
        ResponseEntity<List<Person>> members = restTemplate.exchange(uri, HttpMethod.GET, request, returnType);
        builder = UriComponentsBuilder.fromHttpUrl(ProcessGitHubOrganisation.GITHUB)
                .queryParam("filter", "all")
                .queryParam("per_page", ProcessGitHubOrganisation.PAGES)
                .path("/orgs/{organisation}/outside_collaborators");

        uri = builder.build().expand(organisation).encode().toUri();
        ResponseEntity<List<Person>> collaborators = restTemplate.exchange(uri, HttpMethod.GET, request, returnType);
        List<Person> all = members.getBody();
        all.addAll(collaborators.getBody());
        all.forEach(this::processPersonDetails);
        return all;
    }

    public void processPersonDetails(Person person) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "token " + ProcessGitHubOrganisation.TOKEN);
        headers.add(HttpHeaders.ACCEPT, "application/vnd.github.jean-grey-preview+json");
        HttpEntity request = new HttpEntity(headers);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(ProcessGitHubOrganisation.GITHUB)
                .path("/users/")
                .path(person.getLogin());
        URI uri = builder.build().encode().toUri();
        ResponseEntity<Person> retrieved = restTemplate.exchange(uri, HttpMethod.GET, request, Person.class);
        person.setEmail(retrieved.getBody().getEmail());
        person.setName(retrieved.getBody().getName());
    }

}
