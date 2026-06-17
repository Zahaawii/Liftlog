CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(320) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(120),
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    last_login_at TIMESTAMP NULL,
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_refresh_tokens_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS exercises (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    category VARCHAR(80),
    primary_muscle_group VARCHAR(80),
    measurement_type VARCHAR(40) NOT NULL,
    is_active BOOLEAN NOT NULL,
    source VARCHAR(40) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_exercises_name UNIQUE (name),
    INDEX idx_exercises_category (category),
    INDEX idx_exercises_primary_muscle_group (primary_muscle_group),
    INDEX idx_exercises_is_active (is_active)
);

CREATE TABLE IF NOT EXISTS workouts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    workout_date DATE NOT NULL,
    title VARCHAR(160),
    notes VARCHAR(1000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_workouts_user FOREIGN KEY (user_id) REFERENCES users (id),
    INDEX idx_workouts_user_workout_date (user_id, workout_date),
    INDEX idx_workouts_user_created_at (user_id, created_at)
);

CREATE TABLE IF NOT EXISTS workout_exercises (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workout_id BIGINT NOT NULL,
    exercise_id BIGINT NOT NULL,
    display_order INT NOT NULL,
    notes VARCHAR(1000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_workout_exercises_workout FOREIGN KEY (workout_id) REFERENCES workouts (id) ON DELETE CASCADE,
    CONSTRAINT fk_workout_exercises_exercise FOREIGN KEY (exercise_id) REFERENCES exercises (id),
    INDEX idx_workout_exercises_workout (workout_id),
    INDEX idx_workout_exercises_exercise (exercise_id)
);

CREATE TABLE IF NOT EXISTS workout_sets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workout_exercise_id BIGINT NOT NULL,
    set_number INT NOT NULL,
    reps INT,
    weight DECIMAL(8, 2),
    duration_seconds INT,
    distance DECIMAL(10, 2),
    completed BOOLEAN NOT NULL,
    notes VARCHAR(1000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_workout_sets_workout_exercise FOREIGN KEY (workout_exercise_id) REFERENCES workout_exercises (id) ON DELETE CASCADE,
    INDEX idx_workout_sets_workout_exercise (workout_exercise_id)
);

CREATE TABLE IF NOT EXISTS nutrition_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    log_date DATE NOT NULL,
    meal_type VARCHAR(40) NOT NULL,
    food_name VARCHAR(160) NOT NULL,
    serving_quantity DECIMAL(8, 2),
    calories INT,
    protein DECIMAL(8, 2),
    carbohydrates DECIMAL(8, 2),
    fat DECIMAL(8, 2),
    notes VARCHAR(1000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_nutrition_logs_user FOREIGN KEY (user_id) REFERENCES users (id),
    INDEX idx_nutrition_logs_user_log_date (user_id, log_date),
    INDEX idx_nutrition_logs_user_meal_type_log_date (user_id, meal_type, log_date)
);

CREATE TABLE IF NOT EXISTS goals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    goal_type VARCHAR(40) NOT NULL,
    title VARCHAR(160) NOT NULL,
    target_metric VARCHAR(80) NOT NULL,
    target_value DECIMAL(12, 2) NOT NULL,
    current_baseline DECIMAL(12, 2),
    start_date DATE NOT NULL,
    target_date DATE,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_goals_user FOREIGN KEY (user_id) REFERENCES users (id),
    INDEX idx_goals_user_status (user_id, status),
    INDEX idx_goals_user_goal_type (user_id, goal_type),
    INDEX idx_goals_target_date (target_date)
);

CREATE TABLE IF NOT EXISTS goal_check_ins (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    goal_id BIGINT NOT NULL,
    check_in_date DATE NOT NULL,
    check_in_value DECIMAL(12, 2) NOT NULL,
    notes VARCHAR(1000),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_goal_check_ins_goal FOREIGN KEY (goal_id) REFERENCES goals (id) ON DELETE CASCADE,
    INDEX idx_goal_check_ins_goal_check_in_date (goal_id, check_in_date)
);

CREATE TABLE IF NOT EXISTS ai_feedback (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    request_type VARCHAR(80) NOT NULL,
    provider VARCHAR(80) NOT NULL,
    status VARCHAR(40) NOT NULL,
    prompt_summary VARCHAR(1000) NOT NULL,
    summary VARCHAR(1000) NOT NULL,
    recommendations TEXT,
    feedback TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP NULL,
    error_code VARCHAR(120),
    CONSTRAINT fk_ai_feedback_user FOREIGN KEY (user_id) REFERENCES users (id),
    INDEX idx_ai_feedback_user_created_at (user_id, created_at),
    INDEX idx_ai_feedback_provider_status (provider, status)
);
