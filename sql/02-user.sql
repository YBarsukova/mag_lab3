CREATE USER lab_user WITH PASSWORD 'lab_password';

GRANT CONNECT ON DATABASE bookstore TO lab_user;

GRANT USAGE ON SCHEMA public TO lab_user;

GRANT SELECT, INSERT, UPDATE
      ON ALL TABLES IN SCHEMA public
          TO lab_user;

GRANT USAGE, SELECT
             ON ALL SEQUENCES IN SCHEMA public
                 TO lab_user;