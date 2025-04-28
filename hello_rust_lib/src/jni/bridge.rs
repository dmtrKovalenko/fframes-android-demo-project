use android_logger::{Config, FilterBuilder};
use fframes::{EncoderOptions, RenderOptions, StaticMediaProvider};
use jni::objects::{JClass, JString};
use jni::JNIEnv;
use log::{info, LevelFilter};
use std::path::PathBuf;
use super::hello_world::{HelloWorldMedia, HelloWorldVideo};

#[no_mangle]
pub extern "system" fn Java_com_curtesmalteser_hellorust_MainActivity_render<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    slug: JString<'local>,
    output: JString<'local>,
    tmp_dir: JString<'local>,
) {
    android_logger::init_once(
        Config::default()
            .with_max_level(LevelFilter::Trace) // limit log level
            .with_tag("mytag") // logs will show under mytag tag
            .with_filter(
                // configure messages for specific crate
                FilterBuilder::new()
                    .parse("debug,hello::crate=error")
                    .build(),
            ),
    );
    let media = HelloWorldMedia::prepare().unwrap();
    let slug: String = env
        .get_string(&slug)
        .expect("Failed to convert Java string to Rust string")
        .into();
    let output: String = env
        .get_string(&output)
        .expect("Failed to convert Java string to Rust string")
        .into();
    let tmp_dir: String = env
        .get_string(&tmp_dir)
        .expect("Failed to convert Java string to Rust string")
        .into();

    info!("Rendering video to output: {}", &output);

    fframes::render(
        &output,
        &HelloWorldVideo {
            _media: &media,
            slug: &slug,
        },
        fframes::cpu::CpuRenderingBackend {
            cache_capacity: 5,
            ..Default::default()
        },
        &RenderOptions {
            media: Some(&media),
            load_system_fonts: false,
            logger: fframes::FFramesLoggerVariant::Compact,
            encoder_options: EncoderOptions {
                // hardware accelerated h264 encoder comes with videotoolbox
                preferred_video_codec: Some("h264_mediacodec"),
                tmp_files_directory: Some(&PathBuf::from(tmp_dir)),
                ..Default::default()
            },
            ..Default::default()
        },
    )
    .unwrap();

    info!("Video rendering completed");
}
