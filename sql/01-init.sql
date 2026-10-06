CREATE TABLE authors (
                         id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                         name VARCHAR(255) NOT NULL,
                         country VARCHAR(100)
);

CREATE TABLE books (
                       id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                       title VARCHAR(255) NOT NULL,
                       author_id BIGINT NOT NULL,
                       price NUMERIC(10, 2) NOT NULL CHECK (price >= 0),
                       stock INTEGER NOT NULL DEFAULT 0 CHECK (stock >= 0),

                       CONSTRAINT fk_books_author
                           FOREIGN KEY (author_id)
                               REFERENCES authors(id)
);

CREATE TABLE customers (
                           id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                           name VARCHAR(255) NOT NULL,
                           email VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE orders (
                        id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                        customer_id BIGINT NOT NULL,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        status VARCHAR(50) NOT NULL DEFAULT 'NEW',

                        CONSTRAINT fk_orders_customer
                            FOREIGN KEY (customer_id)
                                REFERENCES customers(id),

                        CONSTRAINT chk_orders_status
                            CHECK (status IN ('NEW', 'PAID', 'SHIPPED', 'COMPLETED', 'CANCELLED'))
);

CREATE TABLE order_items (
                             id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                             order_id BIGINT NOT NULL,
                             book_id BIGINT NOT NULL,
                             quantity INTEGER NOT NULL CHECK (quantity > 0),
                             price NUMERIC(10, 2) NOT NULL CHECK (price >= 0),

                             CONSTRAINT fk_order_items_order
                                 FOREIGN KEY (order_id)
                                     REFERENCES orders(id),

                             CONSTRAINT fk_order_items_book
                                 FOREIGN KEY (book_id)
                                     REFERENCES books(id)
);