const canvas = document.getElementById("gameCanvas");

const ctx = canvas.getContext("2d");

const scoreElement =
    document.getElementById("score");

const bestElement =
    document.getElementById("best");

const overlay =
    document.getElementById("overlay");

const overlayTitle =
    document.getElementById("overlayTitle");

const overlayMessage =
    document.getElementById("overlayMessage");

const startButton =
    document.getElementById("startButton");

const leaderboard =
    document.getElementById("leaderboard");

const GRID_SIZE = 30;

const CELL_SIZE =
    canvas.width / GRID_SIZE;

const START_SPEED = 150;

const MIN_SPEED = 70;

let snake;

let food;

let direction;

let nextDirection;

let score;

let gameTimer = null;

let gameRunning = false;

let best =
    Number(
        localStorage.getItem(
            "acebookSnakeBest"
        ) || 0
    );

bestElement.textContent = best;

function resetGame() {

    snake = [
        { x: 10, y: 10 },
        { x: 9, y: 10 },
        { x: 8, y: 10 }
    ];

    direction = "RIGHT";

    nextDirection = "RIGHT";

    score = 0;

    scoreElement.textContent = score;

    placeFood();

    draw();
}

function startGame() {

    clearInterval(gameTimer);

    resetGame();

    gameRunning = true;

    overlay.classList.add("hidden");

    scheduleNextTick();
}


function scheduleNextTick() {

    clearInterval(gameTimer);

    const speed =
        Math.max(
            MIN_SPEED,
            START_SPEED - score * 2
        );

    gameTimer =
        setInterval(
            tick,
            speed
        );
}


function tick() {

    direction = nextDirection;

    const head = {
        ...snake[0]
    };


    if (direction === "UP") {
        head.y--;
    }

    if (direction === "DOWN") {
        head.y++;
    }

    if (direction === "LEFT") {
        head.x--;
    }

    if (direction === "RIGHT") {
        head.x++;
    }


    if (
        hitsWall(head) ||
        hitsSelf(head)
    ) {

        endGame();

        return;
    }


    snake.unshift(head);

    if (
        head.x === food.x &&
        head.y === food.y
    ) {

        score++;

        scoreElement.textContent = score;


        if (score > best) {

            best = score;

            bestElement.textContent = best;

            localStorage.setItem(
                "acebookSnakeBest",
                best
            );
        }


        placeFood();

        scheduleNextTick();

    } else {

        snake.pop();
    }


    draw();
}

function hitsWall(head) {

    return (
        head.x < 0 ||
        head.x >= GRID_SIZE ||
        head.y < 0 ||
        head.y >= GRID_SIZE
    );
}

function hitsSelf(head) {

    return snake.some(
        segment =>
            segment.x === head.x &&
            segment.y === head.y
    );
}

function placeFood() {

    do {

        food = {
            x: Math.floor(
                Math.random() * GRID_SIZE
            ),

            y: Math.floor(
                Math.random() * GRID_SIZE
            )
        };

    } while (
        snake &&
        snake.some(
            segment =>
                segment.x === food.x &&
                segment.y === food.y
        )
        );
}

function draw() {

    ctx.fillStyle = "#111827";

    ctx.fillRect(
        0,
        0,
        canvas.width,
        canvas.height
    );

    ctx.strokeStyle =
        "rgba(255,255,255,.05)";

    ctx.lineWidth = 1;


    for (
        let i = 0;
        i <= GRID_SIZE;
        i++
    ) {

        ctx.beginPath();

        ctx.moveTo(
            i * CELL_SIZE,
            0
        );

        ctx.lineTo(
            i * CELL_SIZE,
            canvas.height
        );

        ctx.stroke();


        ctx.beginPath();

        ctx.moveTo(
            0,
            i * CELL_SIZE
        );

        ctx.lineTo(
            canvas.width,
            i * CELL_SIZE
        );

        ctx.stroke();
    }

    ctx.fillStyle = "#4f46e5";

    ctx.beginPath();

    ctx.arc(
        food.x * CELL_SIZE +
        CELL_SIZE / 2,

        food.y * CELL_SIZE +
        CELL_SIZE / 2,

        CELL_SIZE * 0.36,

        0,

        Math.PI * 2
    );

    ctx.fill();

    snake.forEach(
        (segment, index) => {

            ctx.fillStyle =
                index === 0
                    ? "#22c55e"
                    : "#16a34a";

            ctx.fillRect(
                segment.x * CELL_SIZE + 1,
                segment.y * CELL_SIZE + 1,
                CELL_SIZE - 2,
                CELL_SIZE - 2
            );
        }
    );
}

function endGame() {

    gameRunning = false;

    clearInterval(gameTimer);

    overlayTitle.textContent =
        "Game Over";

    overlayMessage.textContent =
        `You scored ${score}.`;

    startButton.textContent =
        "Play Again";

    overlay.classList.remove("hidden");

    saveScore(score);

    loadLeaderboard();
}

function setDirection(newDirection) {

    if (!gameRunning) {
        return;
    }


    const opposites = {

        UP: "DOWN",

        DOWN: "UP",

        LEFT: "RIGHT",

        RIGHT: "LEFT"
    };

    if (
        opposites[direction] !==
        newDirection
    ) {

        nextDirection =
            newDirection;
    }
}


document.addEventListener(
    "keydown",
    event => {

        const keys = {

            ArrowUp: "UP",

            ArrowDown: "DOWN",

            ArrowLeft: "LEFT",

            ArrowRight: "RIGHT",

            w: "UP",
            W: "UP",

            s: "DOWN",
            S: "DOWN",

            a: "LEFT",
            A: "LEFT",

            d: "RIGHT",
            D: "RIGHT"
        };


        if (keys[event.key]) {

            event.preventDefault();

            setDirection(
                keys[event.key]
            );
        }
    }
);


document
    .querySelectorAll(
        "[data-direction]"
    )
    .forEach(button => {

        button.addEventListener(
            "click",
            () => {

                setDirection(
                    button.dataset
                        .direction
                        .toUpperCase()
                );
            }
        );
    });

startButton.addEventListener(
    "click",
    startGame
);

async function saveScore(finalScore) {

    if (finalScore <= 0) {
        return;
    }


    try {

        await fetch(
            "/api/games/snake/scores",
            {
                method: "POST",

                headers: {
                    "Content-Type":
                        "application/json"
                },

                body: JSON.stringify({
                    score: finalScore
                })
            }
        );

    } catch (error) {

        console.warn(
            "Could not save Snake score:",
            error
        );
    }
}

async function loadLeaderboard() {

    try {

        const response =
            await fetch(
                "/api/games/snake/scores?limit=10"
            );


        if (!response.ok) {
            throw new Error(
                "Leaderboard request failed"
            );
        }


        const scores =
            await response.json();


        leaderboard.innerHTML = "";


        if (scores.length === 0) {

            leaderboard.innerHTML =
                "<li>No scores yet.</li>";

            return;
        }


        scores.forEach(
            (entry, index) => {

                const li =
                    document.createElement(
                        "li"
                    );

                li.textContent =
                    `${entry.username} — ${entry.score}`;

                leaderboard.appendChild(li);
            }
        );

    } catch (error) {

        leaderboard.innerHTML =
            "<li>Leaderboard unavailable.</li>";
    }
}


resetGame();

loadLeaderboard();