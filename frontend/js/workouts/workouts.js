import { request } from "../api/apiClient.js";
import { showMessage } from "../shared/ui.js";

const workoutForm = document.querySelector("#workout-form");
const exerciseSelect = document.querySelector("#exercise-select");
const workoutList = document.querySelector("#workout-list");
const loadWorkoutsButton = document.querySelector("#load-workouts");

export async function initWorkouts() {
  workoutForm.addEventListener("submit", handleWorkoutSubmit);
  loadWorkoutsButton.addEventListener("click", loadWorkouts);
  setDefaultWorkoutDate();
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
      .map((exercise) => `<option value="${exercise.id}">${escapeHtml(exercise.name)}</option>`)
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
    setDefaultWorkoutDate();
    showMessage("Workout logged.");
    await loadWorkouts();
    notifyFitnessDataChanged();
  } catch (error) {
    showMessage(error.message);
  }
}

function renderWorkouts(workouts) {
  if (!workouts.length) {
    workoutList.innerHTML = `
      <article class="empty-state">
        <h3>No workouts logged yet</h3>
        <p class="metric-subtext">Log a workout to start building your strength progression history.</p>
      </article>
    `;
    return;
  }

  workoutList.innerHTML = workouts.map(renderWorkout).join("");
}

function renderWorkout(workout) {
  const exerciseCount = workout.exercises?.length || 0;
  const totalSets = (workout.exercises || []).reduce((count, entry) => count + (entry.sets?.length || 0), 0);
  const volume = calculateVolume(workout);
  const exerciseLines = (workout.exercises || [])
    .map((entry) => {
      const sets = (entry.sets || [])
        .map((set) => `${set.reps ?? "-"} reps${set.weight ? ` at ${formatNumber(set.weight)}kg` : ""}`)
        .join(", ");
      return `<p><strong>${escapeHtml(entry.exercise?.name || "Exercise")}:</strong> ${escapeHtml(sets)}</p>`;
    })
    .join("");

  return `
    <article class="workout-item">
      <div class="item-row">
        <h3>${escapeHtml(workout.title || "Workout")}</h3>
        <span class="pill strength-pill">${escapeHtml(workout.workoutDate)}</span>
      </div>
      <p class="item-meta">${exerciseCount} exercises · ${totalSets} sets${volume ? ` · ${formatNumber(volume)}kg volume` : ""}</p>
      ${exerciseLines}
      ${workout.notes ? `<p>${escapeHtml(workout.notes)}</p>` : ""}
    </article>
  `;
}

function calculateVolume(workout) {
  return (workout.exercises || []).reduce((total, entry) => {
    return total + (entry.sets || []).reduce((setTotal, set) => {
      const reps = Number(set.reps) || 0;
      const weight = Number(set.weight) || 0;
      return setTotal + reps * weight;
    }, 0);
  }, 0);
}

function setDefaultWorkoutDate() {
  if (!workoutForm.elements.workoutDate.value) {
    workoutForm.elements.workoutDate.value = new Date().toISOString().slice(0, 10);
  }
}

function formatNumber(value) {
  const number = Number(value) || 0;
  return Number.isInteger(number) ? String(number) : number.toFixed(1);
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
