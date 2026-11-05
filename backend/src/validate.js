// Validaciones del lado del servidor. Cada función devuelve { errors, value }:
// value es null cuando hay errores; errors es { campo: mensaje }.
const ISO_DATE = /^\d{4}-\d{2}-\d{2}$/;
const EMAIL = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

const asObject = (body) => (body && typeof body === 'object' && !Array.isArray(body) ? body : {});
const text = (value) => (typeof value === 'string' ? value.trim() : '');

function isValidDate(value) {
  if (typeof value !== 'string' || !ISO_DATE.test(value)) return false;
  const date = new Date(`${value}T00:00:00Z`);
  return !Number.isNaN(date.getTime()) && date.toISOString().slice(0, 10) === value;
}

function todayISO() {
  const d = new Date();
  const pad = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}

const result = (errors, value) => (Object.keys(errors).length ? { errors, value: null } : { errors, value });

function validateRegister(body) {
  const b = asObject(body);
  const errors = {};
  const name = text(b.name);
  const email = text(b.email).toLowerCase();
  if (name.length < 1 || name.length > 60) errors.name = 'El nombre es obligatorio (máx. 60 caracteres)';
  if (!EMAIL.test(email)) errors.email = 'El correo no tiene un formato válido';
  if (typeof b.password !== 'string' || b.password.length < 8) {
    errors.password = 'La contraseña debe tener al menos 8 caracteres';
  }
  return result(errors, { name, email, password: b.password });
}

function validateLogin(body) {
  const b = asObject(body);
  const errors = {};
  const email = text(b.email).toLowerCase();
  if (!email) errors.email = 'El correo es obligatorio';
  if (typeof b.password !== 'string' || b.password === '') errors.password = 'La contraseña es obligatoria';
  return result(errors, { email, password: b.password });
}

function validatePlant(body, today = todayISO()) {
  const b = asObject(body);
  const errors = {};
  const name = text(b.name);
  const species = text(b.species);

  if (name.length < 2 || name.length > 50) errors.name = 'El nombre debe tener entre 2 y 50 caracteres';
  if (species.length < 1 || species.length > 80) errors.species = 'La especie es obligatoria (máx. 80 caracteres)';

  if (!Number.isInteger(b.wateringIntervalDays) || b.wateringIntervalDays < 1 || b.wateringIntervalDays > 60) {
    errors.wateringIntervalDays = 'Los días entre riegos deben ser un entero entre 1 y 60';
  }

  if (!isValidDate(b.plantingDate)) {
    errors.plantingDate = 'La fecha de siembra debe tener formato yyyy-MM-dd';
  } else if (b.plantingDate > today) {
    errors.plantingDate = 'La fecha de siembra no puede ser futura';
  }

  if (!isValidDate(b.lastWateredDate)) {
    errors.lastWateredDate = 'La fecha del último riego debe tener formato yyyy-MM-dd';
  } else if (b.lastWateredDate > today) {
    errors.lastWateredDate = 'La fecha del último riego no puede ser futura';
  } else if (!errors.plantingDate && b.lastWateredDate < b.plantingDate) {
    errors.lastWateredDate = 'El último riego no puede ser anterior a la siembra';
  }

  if (typeof b.needsSun !== 'boolean') errors.needsSun = 'needsSun debe ser verdadero o falso';

  return result(errors, {
    name,
    species,
    wateringIntervalDays: b.wateringIntervalDays,
    plantingDate: b.plantingDate,
    lastWateredDate: b.lastWateredDate,
    needsSun: b.needsSun,
  });
}

module.exports = { validateRegister, validateLogin, validatePlant };
