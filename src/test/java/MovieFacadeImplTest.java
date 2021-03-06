import facade.MovieFacade;
import facade.MovieFacadeImpl;
import model.Movie;
import model.Rating;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MovieFacadeImplTest {

    private MovieFacade movieFacade;
    private List<Movie> movies;


    @BeforeEach
    void beforeEach() throws IOException {
        movieFacade = new MovieFacadeImpl();
        ObjectMapper mapper = new ObjectMapper();
        movies = mapper.readValue(new File("movies.json"), mapper.getTypeFactory().constructCollectionType(List.class, Movie.class));
    }

    @Test
    void averageRating() {
        Double average35 = 71.5;
        assertEquals(average35, movieFacade.averageRating(movies.get(35)));

        Double average0 = 55.416666666666664;
        assertEquals(average0, movieFacade.averageRating(movies.get(0)));

        Double average37 = 52.0;
        assertEquals(average37, movieFacade.averageRating(movies.get(37)));
    }

    @Test
    void longestMovieWithHighRating() {
        Movie result = assertTimeout(Duration.ofMillis(10), () -> movieFacade.longestMovieWithHighRating(60.0, movies));

        assertAll("60.0 as minimum rating",
                () -> {
                    assertNotNull(result);

                    assertAll("Value check", () -> {
                        assertEquals(Duration.ofSeconds(16491), result.getDuration());
                        assertEquals("ad consectetur adipisicing", result.getTitle());
                        assertEquals(7, result.getRatings().size());
                    });
                }
        );

        Movie result2 = assertTimeout(Duration.ofMillis(10), () -> movieFacade.longestMovieWithHighRating(55.0, movies));

        assertAll("55.0 as minimum rating",
            () -> {
                assertNotNull(result2);

                assertAll("Value check", () -> {
                    assertNotEquals(Duration.ofSeconds(16491), result2.getDuration());
                    assertNotEquals(result2, result);
                    assertEquals(12, result2.getRatings().size());
                    assertEquals("ea officia nostrud", result2.getTitle());
                    assertEquals(17856, result2.getDuration().getSeconds());
                });
            }
        );


        Movie result3 = assertTimeout(Duration.ofMillis(5), () -> movieFacade.longestMovieWithHighRating(100.0, movies));
        assertNull(result3);

        Movie result4 = assertTimeout(Duration.ofMillis(10), () -> movieFacade.longestMovieWithHighRating(0.0, movies));
        assertNotNull(result4);
    }

    @Test
    void shortestMovieWithLowRating() {
        Movie result = assertTimeout(Duration.ofMillis(10), () -> movieFacade.shortestMovieWithLowRating(60.0, movies));

        assertAll("60.0 as maximum rating",
                () -> {
                    assertNotNull(result);

                    assertAll("Value check", () -> {
                        assertEquals(Duration.ofSeconds(912), result.getDuration());
                        assertEquals("esse adipisicing adipisicing", result.getTitle());
                        assertEquals(15, result.getRatings().size());
                    });
                }

        );

        Movie result2 = assertTimeout(Duration.ofMillis(10), () -> movieFacade.shortestMovieWithLowRating(45.0, movies));

        assertAll("45.0 as maximum rating",
                () -> {
                    assertNotNull(result2);

                    assertAll("Value check", () -> {
                        assertEquals(Duration.ofSeconds(1870), result2.getDuration());
                        assertEquals(14, result2.getRatings().size());
                        assertEquals("eu ipsum adipisicing", result2.getTitle());
                    });
                }
        );


        Movie result3 = assertTimeout(Duration.ofMillis(5), () -> movieFacade.shortestMovieWithLowRating(100.0, movies));
        assertNotNull(result3);

        Movie result4 = assertTimeout(Duration.ofMillis(10), () -> movieFacade.shortestMovieWithLowRating(0.0, movies));
        assertNull(result4);
    }

    @Test
    void topRatedMovies() {
        List<Movie> movi = movieFacade.topRatedMovies(5, movies);
        for (int i = 0; i < movi.size() - 2; i++) {
            assertTrue(movieFacade.averageRating(movi.get(i)) > movieFacade.averageRating(movi.get(i+1)));
        }
    }

    @Test
    void sortByTimeDescending() {
        List<Movie> sorted = assertTimeout(Duration.ofMillis(50), () -> movieFacade.sortByTimeDescending(movies));

        assertAll("Check if not null", () -> {
            assertNotNull(sorted);

            assertAll("Check if elements are not the same",
                () -> {
                    assertNotEquals(movies.get(0), sorted.get(0));
                    assertNotEquals(movies.get(99), sorted.get(99));

                    assertAll("Check if sorted", () -> {
                        for (int i = 0; i < sorted.size() - 2; i++) {
                            assertTrue(sorted.get(i).getDuration().getSeconds() > sorted.get(i+1).getDuration().getSeconds());
                        }
                    });
                }
            );
        });
    }

    @Test
    void findNRatings() {
        List<Movie> nMovies = movieFacade.findNRatings(2, movies);

        assertAll( "Check num of Mov", () -> {
            assertNotNull(nMovies);
            assertEquals(8, nMovies.size());

            for (Movie mov : nMovies) {
                assertTrue(mov.getRatings().size() <= 2);
            }
        });
    }

    @Test
    void moviesBetweenRatings() {
        List<Movie> moviesBetweenRatings = movieFacade.moviesBetweenRatings(51.0, 89.0, movies);

        assertAll("Check if not null", () -> {
            assertNotNull(moviesBetweenRatings);

            for (Movie mov : moviesBetweenRatings) {
                double avg = movieFacade.averageRating(mov);
                assertThat(avg, allOf(is(lessThan(89.0)), is(not(lessThan(51.0)))));
                //assertTrue(51.0 < avg && avg < 89.0);
            }
        });
    }

    @Test
    void fbRatings() {
        String div3 = "Divisble by 3";
        String div5 = "Divisble by 5";
        String div3and5 = "Divisble by 3 and 5";

        assertAll("Check if comments are changed", () -> {
            for (Rating rate : movieFacade.fbRatings(movies.get(99)).getRatings()) {
                if (rate.getRating() % 15 == 0) {
                    assertEquals(div3and5, rate.getComment());
                } else if (rate.getRating() % 3 == 0) {
                    assertEquals(div3, rate.getComment());
                } else if (rate.getRating() % 5 == 0) {
                    assertEquals(div5, rate.getComment());
                }
            }
        });

    }

    @Test
    void searchByTitle() {
        String title3 = movies.get(3).getTitle();
        assertThat(movieFacade.searchByTitle(title3, movies), is(equalTo(movies.get(3))));

        String title23 = movies.get(23).getTitle();
        assertThat(movieFacade.searchByTitle(title23, movies), is(equalTo(movies.get(23))));

        String title83 = movies.get(83).getTitle();
        assertEquals(movies.get(83), movieFacade.searchByTitle(title83, movies));

    }

    @Test
    void findByKeywords() {
        String[] keywords = {"isti", "non", "nisi", "du", "makkar"};
        List<Movie> keyWordMovies = movieFacade.findByKeywords(keywords, movies);

        assertAll("Check if not null", () -> {
            assertNotNull(keyWordMovies);
            assertEquals(17, keyWordMovies.size());

            for (Movie mov : keyWordMovies) {
                assertTrue(Arrays.stream(keywords).anyMatch(mov.getTitle()::contains));
            }
        });
    }

}