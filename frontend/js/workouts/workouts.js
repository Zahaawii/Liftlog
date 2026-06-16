import { request } from "../api/apiClient.js";
import { showMessage } from "../shared/ui.js";

const workoutForm = document.querySelector("#workout-form");
const exerciseSelect = document.querySelector("#exercise-select");
const workoutList = document.querySelector("#workout-list");
const loadWorkoutsButton = document.querySelector("#load-workouts");

export async function initWorkouts() {
  workoutForm.addEventListener("submit", handleWorkoutSubmit);
  loadWorkoutsButton.addEventListener("click", loadWorkouts);
  workoutList.textContent = "Sign in to view workouts.";
}

export async function loadWorkoutData() {
  await loadExercises();
  await loadWorkouts();
}

export function clearWorkoutData() {
  exerciseSelect.innerHTML = "";
  workoutList.textContent = "Sign in to view workouts.";
}

async function loadExercises() {
  try {
    const exercises = await request("/api/exercises");
    exerciseSelect.innerHTML = exercises
      .map((exercise) => `<option value="${exercise.id}">${exercise.name}</option>`)
      .join("");
  } catch (error) {
    showMessage(error.message);
  }
}

async function loadWorkouts() {
  try {
    const response = await request("/api/workouts?page=0&size=10");
    renderWorkouts(response.items);
  } catch (error) {
    workoutList.textContent = "Sign in to view workouts.";
    showMessage(error.message);
  }
}

async function handleWorkoutSubmit(event) {
  event.preventDefault();
  const formData = new FormData(workoutForm);
  const reps = Number.parseInt(formData.get("reps"), 10);
  const weightValue = formData.get("weight");

  const payload = {
    workoutDate: formData.get("workoutDate"),
    title: formData.get("title"),
    notes: formData.get("notes"),
    exercises: [
      {
        exerciseId: Number.parseInt(formData.get("exerciseId"), 10),
        sets: [
          {
            setNumber: 1,
            reps,
            weight: weightValue ? Number.parseFloat(weightValue) : null,
            completed: true
          }
        ]
      }
    ]
  };

  try {
    await request("/api/workouts", {
      method: "POST",
      body: JSON.stringify(payload)
    });
    workoutForm.reset();
    showMessage("Workout logged.");
    await loadWorkouts();
    notifyFitnessDataChanged();
  } catch (error) {
    showMessage(error.message);
  }
}

function renderWorkouts(workouts) {
  if (!workouts.length) {
    workoutList.textContent = "No workouts logged yet.";
    return;
  }

  workoutList.innerHTML = workouts.map(renderWorkout).join("");
}

function renderWorkout(workout) {
  const exerciseLines = workout.exercises
    .map((entry) => {
      const sets = entry.sets
        .map((set) => `${set.reps ?? "-"} reps${set.weight ? ` at ${set.weight}` : ""}`)
        .join(", ");
      return `<p>${escapeHtml(entry.exercise.name)}: ${escapeHtml(sets)}</p>`;
    })
    .join("");

  return `
    <article class="workout-item">
      <h3>${escapeHtml(workout.title || "Workout")} · ${escapeHtml(workout.workoutDate)}</h3>
      ${exerciseLines}
      ${workout.notes ? `<p>${escapeHtml(workout.notes)}</p>` : ""}
    </article>
  `;
}

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll("\"", "&quot;")
    .replaceAll("'", "&#039;");
}

function notifyFitnessDataChanged() {
  window.dispatchEvent(new CustomEvent("liftlog:data-changed"));
}
