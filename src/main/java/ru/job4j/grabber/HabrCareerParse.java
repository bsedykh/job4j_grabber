package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {
    private static final int PAGES_NUM = 5;
    private static final String PREFIX = "/vacancies?page=";
    private static final String SUFFIX = "&q=Java%20developer&type=all";

    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    @Override
    public List<Post> list(String link) {
        List<Post> posts = new ArrayList<>();
        try {
            for (int page = 1; page <= PAGES_NUM; page++) {
                String fullLink = "%s%s%d%s".formatted(link, PREFIX, page, SUFFIX);
                Connection connection = Jsoup.connect(fullLink);
                Document document = connection.get();
                Elements rows = document.select(".vacancy-card__inner");
                for (Element row : rows) {
                    Element dateElement = row.select(".vacancy-card__date").first().child(0);
                    Element titleElement = row.select(".vacancy-card__title").first();
                    Element linkElement = titleElement.child(0);
                    String postLink = String.format("%s%s", link, linkElement.attr("href"));
                    posts.add(new Post(
                            0,
                            titleElement.text(),
                            postLink,
                            retrieveDescription(postLink),
                            dateTimeParser.parse(dateElement.attr("datetime"))
                    ));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return posts;
    }

    private String retrieveDescription(String link) throws IOException {
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        Element description = document.select(".vacancy-description__text").first();
        return description.text();
    }
}
