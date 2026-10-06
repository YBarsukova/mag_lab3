INSERT INTO authors (name, country) VALUES
                                        ('George Orwell', 'United Kingdom'),
                                        ('J.R.R. Tolkien', 'United Kingdom'),
                                        ('Frank Herbert', 'United States'),
                                        ('Fyodor Dostoevsky', 'Russia');

INSERT INTO books (title, author_id, price, stock) VALUES
                                                       ('1984', 1, 899.00, 12),
                                                       ('Animal Farm', 1, 649.00, 8),
                                                       ('The Hobbit', 2, 1099.00, 5),
                                                       ('The Lord of the Rings', 2, 1899.00, 3),
                                                       ('Dune', 3, 1299.00, 7),
                                                       ('Crime and Punishment', 4, 999.00, 10);

INSERT INTO customers (name, email) VALUES
                                        ('Anna Petrova', 'anna@example.com'),
                                        ('Ivan Sidorov', 'ivan@example.com'),
                                        ('Maria Volkova', 'maria@example.com');

INSERT INTO orders (customer_id, status) VALUES
                                             (1, 'PAID'),
                                             (2, 'NEW'),
                                             (1, 'SHIPPED');

INSERT INTO order_items (order_id, book_id, quantity, price) VALUES
                                                                 (1, 1, 1, 899.00),
                                                                 (1, 3, 1, 1099.00),
                                                                 (2, 5, 2, 1299.00),
                                                                 (3, 2, 1, 649.00);