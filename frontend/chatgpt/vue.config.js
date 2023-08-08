const webpack = require('webpack');

module.exports = {
  transpileDependencies: true,
  lintOnSave: false,
  chainWebpack: config => {
    config.plugin('html').tap(args => {
      args[0].favicon = './src/assets/logo.jpg';
      return args;
    });
  },
  configureWebpack: {
    plugins: [
      new webpack.DefinePlugin({
        'process.env': {
          API_BASE_URL: JSON.stringify(process.env.VUE_APP_API_BASE_URL)
        }
      })
    ]
  }
};
