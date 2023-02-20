package fr.uga.l3miage.library.books;

import fr.uga.l3miage.library.authors.AuthorDTO;
import fr.uga.l3miage.library.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping(value = "/api", produces = "application/json")
public class BooksController {

    private final BookService bookService;
    private final BooksMapper booksMapper;

    @Autowired
    public BooksController(BookService bookService, BooksMapper booksMapper) {
       this.bookService = bookService;
        this.booksMapper = booksMapper;
    }

    @GetMapping("/books/v1")
    public Collection<BookDTO> books(@RequestParam("q") String query) {
        return null;
    }

    @GetMapping("/bookId/v1")
    public BookDTO book(@RequestParam("q") Long id) {
        return null;
    }

    @PostMapping("/NewBook/v1")
    public BookDTO newBook(Long authorId, BookDTO book) {
        return null;
    }

    @PostMapping("/UpdateBook/v1")
    public BookDTO updateBook(Long authorId, BookDTO book) {
        // attention BookDTO.id() doit être égale à id, sinon la requête utilisateur est mauvaise
        return null;
    }

    @PostMapping("/DeleteBook/v1")
    public void deleteBook(Long id) {

    }

    @PostMapping("/AddAuthor/v1")
    public void addAuthor(Long authorId, AuthorDTO author) {

    }
}
