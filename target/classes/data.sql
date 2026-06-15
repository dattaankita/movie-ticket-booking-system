-- USERS

INSERT INTO users(id, email, full_name, role, created_at, updated_at)
VALUES
(1,'admin@test.com','System Admin','ADMIN',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
(2,'john@test.com','John Doe','CUSTOMER',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
(3,'alice@test.com','Alice Doe','CUSTOMER',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);

-- CITY

INSERT INTO cities(id,name,created_at,updated_at)
VALUES
(1,'Bangalore',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);

-- THEATER

INSERT INTO theaters(id,city_id,name,created_at,updated_at)
VALUES
(1,1,'PVR Phoenix Mall',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);

-- PRICING TIERS

INSERT INTO pricing_tiers(
 id,
 name,
 tier_type,
 multiplier,
 active,
 created_at,
 updated_at
)
VALUES
(1,'Weekend Premium','WEEKEND',1.20,true,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);

-- REFUND POLICIES

INSERT INTO refund_policies(
 id,
 name,
 hours_before_show,
 refund_percentage,
 active,
 created_at,
 updated_at
)
VALUES
(1,'Full Refund',24,100,true,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
(2,'Partial Refund',2,50,true,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);

-- DISCOUNT CODES

INSERT INTO discount_codes(
 id,
 code,
 discount_type,
 discount_value,
 valid_from,
 valid_until,
 max_uses,
 used_count,
 active,
 created_at,
 updated_at
)
VALUES
(
 1,
 'WELCOME10',
 'PERCENTAGE',
 10,
 CURRENT_TIMESTAMP,
 DATEADD('DAY',30,CURRENT_TIMESTAMP),
 1000,
 0,
 true,
 CURRENT_TIMESTAMP,
 CURRENT_TIMESTAMP
);

-- SEATS

INSERT INTO seats(
 id,
 theater_id,
 row_label,
 seat_number,
 category,
 created_at,
 updated_at
)
VALUES
(1,1,'A',1,'REGULAR',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
(2,1,'A',2,'REGULAR',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
(3,1,'A',3,'REGULAR',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
(4,1,'A',4,'REGULAR',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
(5,1,'A',5,'REGULAR',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),

(6,1,'B',1,'PREMIUM',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
(7,1,'B',2,'PREMIUM',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
(8,1,'B',3,'PREMIUM',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
(9,1,'B',4,'PREMIUM',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
(10,1,'B',5,'PREMIUM',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);

-- SHOW

INSERT INTO shows(
 id,
 theater_id,
 movie_title,
 start_time,
 end_time,
 base_price,
 pricing_tier_id,
 status,
 created_at,
 updated_at
)
VALUES
(
 1,
 1,
 'Mission Impossible',
 DATEADD('HOUR',6,CURRENT_TIMESTAMP),
 DATEADD('HOUR',9,CURRENT_TIMESTAMP),
 250,
 1,
 'SCHEDULED',
 CURRENT_TIMESTAMP,
 CURRENT_TIMESTAMP
);

-- SHOW SEATS

INSERT INTO show_seats(show_id,seat_id) VALUES
(1,1),(1,2),(1,3),(1,4),(1,5),
(1,6),(1,7),(1,8),(1,9),(1,10);