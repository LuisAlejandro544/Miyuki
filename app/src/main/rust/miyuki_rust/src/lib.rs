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
