CREATE TABLE transactions (
                              id UUID PRIMARY KEY,
                              description VARCHAR(255) NOT NULL,
                              amount NUMERIC(15,2) NOT NULL,
                              type VARCHAR(30) NOT NULL,
                              source VARCHAR(30) NOT NULL,
                              timestamp TIMESTAMP NOT NULL,

                              user_id UUID NOT NULL,

                              CONSTRAINT fk_transactions_user
                                  FOREIGN KEY (user_id)
                                      REFERENCES users (id)
);

