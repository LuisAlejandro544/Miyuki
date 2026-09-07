use std::os::raw::c_char;

// Returns the version of the Rust Miyuki Core Engine
#[no_mangle]
pub extern "C" fn rust_get_version() -> *const c_char {
    static VERSION: &[u8] = b"Rust Miyuki Geometry Engine v1.0.0 (Earring & Math Core)\0";
    VERSION.as_ptr() as *const c_char
}

// Calculate the fringe bead counts for each column of a Miyuki fringe earring (zarcillo)
// Styles:
// 0: Classic V-Shape (longest in center)
// 1: Inverted V (longest on edges)
// 2: Wave Cascade (harmonic sine curve)
// 3: Diagonal Step Angle
// 4: Diamond / Double Taper
#[no_mangle]
pub extern "C" fn rust_calculate_earring_fringes(
    base_width: i32,
    max_fringe_len: i32,
    min_fringe_len: i32,
    style: i32,
    out_lengths: *mut i32,
    max_count: i32,
) -> i32 {
    if out_lengths.is_null() || base_width <= 0 || max_count < base_width {
        return 0;
    }

    let n = base_width as usize;
    let max_len = max_fringe_len.max(4);
    let min_len = min_fringe_len.max(1).min(max_len);
    let span = (max_len - min_len).max(1) as f32;

    let center = (n - 1) as f32 / 2.0;

    for i in 0..n {
        let frac = if n > 1 {
            (i as f32) / ((n - 1) as f32)
        } else {
            0.5
        };

        let calculated_len: i32 = match style {
            0 => {
                let dist_from_center = (i as f32 - center).abs() / center.max(1.0);
                let height = 1.0 - dist_from_center;
                (min_len as f32 + span * height.max(0.0)).round() as i32
            }
            1 => {
                let dist_from_center = (i as f32 - center).abs() / center.max(1.0);
                (min_len as f32 + span * dist_from_center.min(1.0)).round() as i32
            }
            2 => {
                let angle = frac * std::f32::consts::PI * 2.0;
                let wave = (angle.sin() + 1.0) / 2.0;
                (min_len as f32 + span * wave).round() as i32
            }
            3 => {
                (min_len as f32 + span * frac).round() as i32
            }
            _ => {
                let angle = frac * std::f32::consts::PI * 4.0;
                let diamond = angle.cos().abs();
                (min_len as f32 + span * diamond).round() as i32
            }
        };

        unsafe {
            *out_lengths.add(i) = calculated_len.max(1);
        }
    }

    base_width
}

// Calculate the number of triangular decrease rows for brick stitch earring tops
#[no_mangle]
pub extern "C" fn rust_calculate_triangle_decrease_rows(base_width: i32) -> i32 {
    if base_width <= 1 {
        return 1;
    }
    base_width
}

// Analyzes horizontal and vertical gradient projections of a cropped bead pattern chart
// to estimate the number of columns and rows.
#[no_mangle]
pub extern "C" fn rust_analyze_chart_grid(
    pixels: *const u32,
    width: i32,
    height: i32,
    out_cols: *mut i32,
    out_rows: *mut i32,
) -> i32 {
    if pixels.is_null() || width < 20 || height < 20 || out_cols.is_null() || out_rows.is_null() {
        return 0;
    }

    let w = width as usize;
    let h = height as usize;

    let lum = |px: u32| -> f32 {
        let r = ((px >> 16) & 0xFF) as f32;
        let g = ((px >> 8) & 0xFF) as f32;
        let b = (px & 0xFF) as f32;
        0.299 * r + 0.587 * g + 0.114 * b
    };

    // Calculate vertical gradients summed per column: indicates column separator lines
    let mut col_grad = vec![0.0f32; w];
    for y in 0..h {
        let row_start = y * w;
        for x in 1..w {
            unsafe {
                let p1 = *pixels.add(row_start + x);
                let p0 = *pixels.add(row_start + x - 1);
                let diff = (lum(p1) - lum(p0)).abs();
                col_grad[x] += diff;
            }
        }
    }

    // Calculate horizontal gradients summed per row: indicates row separator lines
    let mut row_grad = vec![0.0f32; h];
    for y in 1..h {
        let row_curr = y * w;
        let row_prev = (y - 1) * w;
        for x in 0..w {
            unsafe {
                let p1 = *pixels.add(row_curr + x);
                let p0 = *pixels.add(row_prev + x);
                let diff = (lum(p1) - lum(p0)).abs();
                row_grad[y] += diff;
            }
        }
    }

    // Estimate column count in expected range (8 to 70)
    let mut best_cols = 20;
    let mut best_col_score = -1.0f32;
    for test_cols in 8..=70 {
        let step = (w as f32) / (test_cols as f32);
        if step < 3.0 { break; }
        let mut score = 0.0f32;
        for c in 1..test_cols {
            let x = (c as f32 * step).round() as usize;
            if x < w {
                score += col_grad[x];
            }
        }
        score /= (test_cols - 1) as f32;
        if score > best_col_score {
            best_col_score = score;
            best_cols = test_cols;
        }
    }

    // Estimate row count in expected range (12 to 120)
    let mut best_rows = 40;
    let mut best_row_score = -1.0f32;
    for test_rows in 12..=120 {
        let step = (h as f32) / (test_rows as f32);
        if step < 3.0 { break; }
        let mut score = 0.0f32;
        for r in 1..test_rows {
            let y = (r as f32 * step).round() as usize;
            if y < h {
                score += row_grad[y];
            }
        }
        score /= (test_rows - 1) as f32;
        if score > best_row_score {
            best_row_score = score;
            best_rows = test_rows;
        }
    }

    unsafe {
        *out_cols = best_cols;
        *out_rows = best_rows;
    }

    1
}

